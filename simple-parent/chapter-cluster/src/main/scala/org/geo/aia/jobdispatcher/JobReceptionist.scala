package org.geo.aia.jobdispatcher

import akka.actor.{Props,Actor,ActorSystem,ActorLogging,ActorRef}
import akka.actor.SupervisorStrategy
import java.net.URLEncoder
import akka.actor.Terminated
import akka.actor.ActorContext

object JobReceptionist {
  def props = Props( new JobReceptionist)
  def name = "receptionist"
  
  case class JobRequest(name: String, text:List[String])
  
  sealed trait Response
  case class JobSuccess(name:String, map: Map[String,Int]) extends Response
  case class JobFailure(name: String) extends Response
  
  case class WordCount(name: String, map: Map[String,Int])
  
  case class Job(name: String, text: List[String], respondTo: ActorRef,
      jobMaster: ActorRef)
}

class JobReceptionist extends Actor with ActorLogging
with CreateMaster {
  import JobReceptionist._
  import JobMaster.StartJob
  import context._
  
  /**
   * JobReceptionist constructor area
   */
  log.info("JobReceptionist coming up")
  
  /**
   * Supervisor Strategy is 
   * stoppingStrategy 
   * Closer to the Erlang way is the strategy to stop children when 
   * they fail and then take corrective action in the supervisor when 
   * DeathWatch signals the loss of the child. This strategy is also 
   * provided pre-packaged as SupervisorStrategy.stoppingStrategy with 
   * an accompanying StoppingSupervisorStrategy configurator to be used 
   * when you want the "/user" guardian to apply it.
   * 
   */
  override def supervisorStrategy: SupervisorStrategy = 
    SupervisorStrategy.stoppingStrategy 
    
    /**
     * The Set of Jobs currently running
     */
    var jobs = Set[Job]()
    var retries = Map[String,Int]()
    val maxRetries = 3 
    
    def receive = {
    /**
     * note the jr here is not really needed since no reference is made 
     * to the object as a hole.
     * This seems to be the entry point in which a new job
     * is created.Message of type JobRequest sends in the 
     * job specs to be processed.
     * A particular Job Name and the text are passed in. 
     */
    case jr@JobRequest(name,text) => 
      log.info(s"Received job $name") 
      
      /**
       * When the job comes in a master is created
       * for the Job soon to be created workers. The master
       * is named: master-<jobname> 
       */
      val masterName = "master-" + URLEncoder.encode(name, "UTF8")
      log.info("new job request just came in {}", masterName )
      /**
       * Create a JobMaster to supervise this worker 
       */
      log.info("calling createMaster {}", masterName)
      val jobMaster  = createMaster(masterName)
      /**
       * Store the name, the text, the client actorRef and the master in the 
       * Job object and then maintain it in a list of jobs
       * 
       */
      log.info("creating Job Object with:")
      log.info("name = {}", name)
      log.info("text = {}", text)
      log.info("sender = {}", sender)
      log.info("jobMaster = {}", jobMaster)
      val job = Job(name,text,sender,jobMaster)
      /**
       * save the new job in the list of jobs being processed
       */
      log.info("saving current job in list")
      jobs = jobs + job 
      
      /**
       * The newly created job master is sent the StartJob
       * message containing the name of the job and 
       * the job itself , which in this case is a text
       * String 
       */
      
      log.info("sending StartJob message to JobMaster, name = {}",name)
      jobMaster ! StartJob(name,text)
      /**
       * what the jobMaster for termination
       * The JobReceptionsit will watch every jobmaster
       * created so that it can supervise its death.
       */
      watch(jobMaster)
      
    case WordCount(jobName,map) =>
      log.info(s"Job $jobName complete.")
      log.info(s"result:${map}")
      jobs.find(_.name == jobName).foreach {
        job =>
          job.respondTo ! JobSuccess(jobName,map)
          stop(job.jobMaster)
          jobs = jobs - job 
      }
      
    case Terminated(jobMaster) =>
      jobs.find(_.jobMaster == jobMaster).foreach {
        failedJob =>
          log.error(s"Job Master $jobMaster terminated before finishing job.")
          
          val name = failedJob.name
          log.error(s"Job ${name} failed.")
          val nrOfRetries = retries.getOrElse(name, 0)
          
          if (maxRetries > nrOfRetries) {
            if(nrOfRetries == maxRetries -1) {
            // Simulating that the Job worker will work just before max retries
            val text = failedJob.text.filterNot(_.contains("FAIL"))
            self.tell(JobRequest(name,text), failedJob.respondTo)
              
            } else {
              self.tell(JobRequest(name, failedJob.text), failedJob.respondTo)
            }
            
            retries = retries + retries.get(name).map(r => name -> (r + 1))
            .getOrElse(name -> 1)
          }
      }
  }
  
} // end of class JobReceptionist
import Utils._
trait CreateMaster {
  def context: ActorContext
  def createMaster(name: String) = {
    peep("creating actor:" + name)
    context.actorOf(JobMaster.props, name)
  }
}
