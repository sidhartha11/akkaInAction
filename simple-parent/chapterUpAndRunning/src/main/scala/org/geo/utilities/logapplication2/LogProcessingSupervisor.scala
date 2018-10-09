package org.geo.utilities.logapplication2

import akka.actor.{Props,Actor,ActorLogging,ActorRef,Terminated,AllForOneStrategy}
import akka.actor.SupervisorStrategy.{ Stop, Resume, Restart, Escalate }

object LogProcessingSupervisor {
  /**
   * The props function simply inializes a Props object for role as an 
   * argument to system.actorOf.
   * The name function is the second argumet to system.actorOf 
   */
  def props(sources: Vector[String],databaseUrls: Vector[String]) = 
    Props(new LogProcessingSupervisor(sources,databaseUrls))
    def name = "file-watcher-supervisor"
}
/**
 * This class plays the role of supervisor for the FileWatchers. It takes
 * a vector of files/dirs and a vector of databaseUrls.
 * Being an Actor, it must extend the Actor trait and also the ActorLogging trait 
 * for logging. 
 */
class LogProcessingSupervisor(
    sources: Vector[String]
    ,databaseUrls: Vector[String]
    ) extends Actor with ActorLogging
    {
    log.info(myName + " constructor")
    import org.geo.utilities.exceptions.CustomExceptions._

  /**
   * Start the file watchers, one for each file passed in
   * Each file watcher is instantiated with a copy of the databaseUrls
   * that we connect to. Each file watcher created will be monitored
   * by this supervisor.
   */
  var fileWatchers: Vector[ActorRef] = sources.map {
    source => 
    log.info(myName + " processing %s".format(source))
    /**
     * each FileWatcher actor is instantiated with a source and vector of 
     * database Urls. This FileWatcher actor will be a child of this actor 
     * since it is using the context to create the actor. 
     */
    val fileWatcher = context.actorOf(
        Props(new FileWatcher(source,databaseUrls))
        )
        log.info(myName + " watching " + fileWatcher)
        context.watch(fileWatcher)
        fileWatcher
  }
  
  /** 
   *  implement the supervisor strategy 
   *  The supervisorStrategy here is AllForOneStrategy which means that 
   *  if one FileWatcher throws a DiskError, then all FileWatchers will 
   *  be stopped, causing a Terminated message to be sent to the 
   *  supervisor. Once all filewatchers finish notifying the supervisor,
   *  the actor system will be terminated by this supervisor. 
   *  
   */
  override def supervisorStrategy = AllForOneStrategy() {
    case _: DiskError => 
      log.info(myName + " DiskError matched")
      Stop
  }
  def receive = {
      case Terminated(fileWatcher) => 
        log.info(myName + " Terminated message " + fileWatcher.toString)
        fileWatchers = fileWatchers.filterNot(_ == fileWatcher )
        if (fileWatchers.isEmpty) {
          log.info("shutting down, all file watchers have failed.")
          context.system.terminate()
        }
    }
  def myName = "LogProcessingSupervisor"
  }