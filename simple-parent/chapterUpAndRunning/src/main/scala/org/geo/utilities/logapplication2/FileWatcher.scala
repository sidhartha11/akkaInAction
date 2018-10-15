package org.geo.utilities.logapplication2

import akka.actor.{ Actor, ActorLogging, OneForOneStrategy, PoisonPill, Terminated,Identify, ActorIdentity }
import akka.actor.SupervisorStrategy.{ Stop, Resume, Restart, Escalate }
import java.io.File
import java.net.URI

import org.geo.utilities.Geoutils._
/**
 * @author george
 * FileWatcher actor is supervised by the LogProcessingSupervisor.
 * The FileWatcher looks out for files to be processed. It gets the following
 * messages:
 * NewFile(file,time)  a file to process , sends a message to LogProcessor
 * SourceAbandoned(uri) if uri is the file this watcher sent , then finish.
 * Terminated(`LogProcessor`)  the log processor notified this watcher of its death.
 *
 *
 */

class FileWatcher(source: String, databaseUrls: Vector[String])
  extends Actor  with FileWatchingAbilities {

  /** import custom exceptions */
  import org.geo.utilities.exceptions.CustomExceptions._
  import FileWatcher._

  emit(myName + " Constructor:source=%s\ndburls=%s".format(source,databaseUrls),true)
  /**
   * This functionality must register the source in some TDB way.
   */
  // register(source)
  self ! NewFile(register(source),1000)

  /**
   * If a file is determined to be corrupted, then just resume
   * processing. This indicates that the log processor that got
   * the corrupted file will continue to process the remaining records ( ?? )
   * Not sure how that will work out yet.
   */
  override def supervisorStrategy = OneForOneStrategy() {
    case _: CorruptedFileException => 
      emit(myName + " supervisor Resuming ",true)
      Resume
  }

  /** start up the child LogProcessor **/
  /** this logProcessor is supervised by this FileWatcher. **/
  /** All the databaseUrls are passed as a constructor argument **/
  val logProcessor = context.actorOf(
    LogProcessor.props(databaseUrls), LogProcessor.name)
  /** monitor this LogProcessor for termination **/
  context.watch(logProcessor)
  /** death watch **/

  import FileWatcher._

  def receive = {
    
    case NewFile(file, _) =>
      emit(myName + " NewFile(%s,_)".format(file),true)
      logProcessor ! LogProcessor.LogFile(file)
    case SourceAbandoned(uri) if uri == source =>
      emit(myName + "%s abandoned, stopping the file watcher.".format(uri),true)
      self ! PoisonPill
    case Terminated(`logProcessor`) =>
      emit(myName + " Log processor terminated, stopping file watcher.",true)
      self ! PoisonPill
  }

  def myName = "FileWatcher"

}

/** companion object of FileWatcher **/
object FileWatcher {
  case class NewFile(file: File, timeAdded: Long)
  case class SourceAbandoned(uri: String)
}

/**
 * Not sure how this trait should be used. One possibility is that the register
 * process should locate a file and send the NewFile message to FileWatcher.
 */
trait FileWatchingAbilities extends ActorLogging {
  self: Actor => 
  import org.geo.utilities.Geoutils._
  import FileWatcher._
  def register(uri: String): File = {
  emit ( "FileWatchingAbilities --> registering %s".format(uri),true)
  val uriF = new URI(uri);
  emit("FileWatchingAbilities --> uriF=%s".format(uriF),true)
  val f = new File(uriF);
  emit( "FileWatchingAbilities --> created File Object:%s".format(f),true)
  // val f =  getFile(uri)
  f
  }

}