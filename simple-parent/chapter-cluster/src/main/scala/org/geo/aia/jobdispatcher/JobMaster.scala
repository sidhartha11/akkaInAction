package org.geo.aia.jobdispatcher

import scala.concurrent.duration._
import akka.actor.Props
import akka.routing.BroadcastPool
import akka.cluster.routing.ClusterRouterPool
import akka.actor.ActorRef
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.SupervisorStrategy
import akka.actor.Cancellable
import akka.actor.ReceiveTimeout
import akka.actor.Terminated

import org.slf4j.LoggerFactory


object  JobMaster {
  def props = Props( new JobMaster)
  case class StartJob(name: String, tet: List[String])
  case class Enlist(worker: ActorRef)
  
  case object NextTask
  case class TaskResult(map: Map[String, Int])
  
  case object Start
  case object MergeResults
  
}

class JobMaster extends Actor 
    with ActorLogging
    with CreateWorkerRouter {
  import JobReceptionist.WordCount
  import JobMaster._
  import JobWorker._
  import context._
  
  var textParts = Vector[List[String]]() 
  var intermediateResult = Vector[Map[String,Int]]()
  var workGiven = 0 
  var workReceived = 0 
  var workers = Set[ActorRef]()
  
  /**
   * This is used to dynamically create a router for every 
   * JobMaster is created for each job.
   */
  val router = createWorkerRouter
  
  override def supervisorStrategy: SupervisorStrategy = 
    SupervisorStrategy.stoppingStrategy 
    
    def receive = idle
    
    def idle: Receive = {
    case StartJob(jobName, text) =>
      /**
       * group the data into lists containing 10 elements each
       */
      log.info("StartJob Message Received," +
          "jobName = {} , text = {}",jobName, text)
      
      textParts = text.grouped(10).toVector 
      log.info("textParts = {}", textParts)
      /**
       * Every 1 second send a message, Work(jobName,self) to the router
       * Scheduler returns a Cancellable Object 
       */
      val cancellable = context.system.scheduler.schedule(0 millis, 1000 millis,
          router, Work(jobName,self))
      context.setReceiveTimeout(60 seconds)
      /**
       * change state and become the working Recieve
       */
      become(working(jobName, sender, cancellable))
  }
  
  def working(jobName: String , 
      receptionist: ActorRef , 
      cancellable: Cancellable): Receive = {
    
    case Enlist(worker) =>
      log.info("Enlisting worker {}", worker)
      watch(worker)
      workers = workers + worker
      
    case NextTask => 
      log.info("NextTask request from worker")
      if (textParts.isEmpty) {
        log.info("textParts.isEmpty == true")
        sender() ! WorkLoadDepleted
      } else {
        log.info("sending Task({} , {}) message to worker", textParts.head, self)
        sender() ! Task(textParts.head, self)
        workGiven = workGiven + 1 
        textParts = textParts.tail
        log.info("remaining list = {}",textParts)
      }
    /**
     * :+ appends to the map
     * +: adds to the beginning of the map 
     */
    case TaskResult(countMap) => 
      log.info("TaskResult Message countMap = {}", countMap)
      intermediateResult = intermediateResult :+ countMap 
      workReceived = workReceived + 1 
      
      if ( textParts.isEmpty && workGiven == workReceived ) {
        cancellable.cancel()
        become(finishing(jobName, receptionist, workers))
        setReceiveTimeout(Duration.Undefined)
        log.info("sending self MergeResults")
        self ! MergeResults
      }
      
    case ReceiveTimeout =>
      if ( workers.isEmpty ) {
        log.info(s"No workers responded in time, Cancelling job $jobName.")
        stop(self)
      } else {
        setReceiveTimeout(Duration.Undefined)
      }
      
    case Terminated(worker) => 
      log.info(s"Worker $worker got terminated. Cancelling job $jobName.")
      stop(self)
  }
  
  def finishing(jobName: String , 
      receptionist: ActorRef , 
      workers: Set[ActorRef]): Receive = {
    
    case MergeResults =>
      val mergedMap = merge()
      workers.foreach(stop(_))
      receptionist ! WordCount(jobName, mergedMap )
      
    case Terminated(worker) => 
      log.info(s"Job $jobName is finishing. Worker ${worker.path.name} is stopped.")
  }
  
  def merge(): Map[String, Int] = {
    intermediateResult.foldLeft(Map[String,Int]()) {
      ( el , acc ) =>
        el.map {
          case (word, count) =>
            acc.get(word).map(accCount => ( word -> (accCount + count)))
            .getOrElse(word -> count)
        } ++ ( acc -- el.keys)
    }
  }
}

/**
 * The JobMaster needs to first create the JobWorkers and then broadcast 
 * the Work message to them. We’ll use a router with a BroadcastPoolRouterConfig to 
 * communicate with the JobWorkers.
 */

trait CreateWorkerRouter {
  
  /**
   * needs to mixin with actor 
   * This creates a router within the cluster using a 
   * BroadcastPool of 10. The actors within the pool are of type:JobWorder.
   * The name of the ClusterRouterPool is worker-router.
   * The job workers are dynamically created and killed after the work is done.
   */
  this: Actor => 
    val logger = LoggerFactory.getLogger("CreateWorkerRouter")

    logger.info(">>>>>Instantiating trait CreateWorkerRouter")
    def createWorkerRouter: ActorRef = {
      logger.info("creating a ClusterRouterPool of workers")
      context.actorOf(
          ClusterRouterPool(BroadcastPool(10), 
              ClusterRouterPoolSettings(
                  totalInstances = 1000,
                  maxInstancesPerNode = 20 , 
                  allowLocalRoutees = false,
                  useRole = None
              )
          ).props(Props[JobWorker]),
          name = "worker-router")
    }
}