package org.geo.utilities.logapplication2

import akka.actor.{ PoisonPill, Props, ActorLogging, OneForOneStrategy, Actor, Terminated }
import akka.actor.SupervisorStrategy.{ Stop, Resume, Restart, Escalate }

import java.util.UUID
import java.io.File
import scala.io.Source

import org.geo.utilities.Geoutils._

/**
 * The LogProcessor plays the role of supervisor for the DbWriters. They are
 * monitored by the LogProcessor.
 */
object LogProcessor {

  def props(databaseUrls: Vector[String]) =
    Props(new LogProcessor(databaseUrls))

  def name = s"log_processor_${UUID.randomUUID.toString}"
  // represents a new log file
  case class LogFile(file: File)
}

class LogProcessor(databaseUrls: Vector[String])
  extends Actor with LogParsing {
  require(databaseUrls.nonEmpty)
  /** import custom exceptions **/
  import org.geo.utilities.exceptions.CustomExceptions._
  val initialDatabaseUrl = databaseUrls.head
  var alternateDatabases = databaseUrls.tail

  emit(myName + " Constructor",true)
  /**
   * OVERRIDE SUPERVISOR STRATEGY
   * pass in a custom Decision partial function (?)
   * There is a one-one mapping between LogProcessor and DbWriter.
   *
   */
  override def supervisorStrategy = OneForOneStrategy() {
    case _: DbBrokenConnectionException =>
      log info s"LogProcessor:supervisor:DbRokenConnectionException detected"
      Restart
    case _: DbNodeDownException =>
      log info s"LogProcessor:supervisor:DbNodeDownException detected"

      Stop
  }

  /** create a dbWriter as child of the LogProcessor **/
  log.info(myName + " creating DbWriter ")
  var dbWriter = context.actorOf(
    DbWriter.props(initialDatabaseUrl),
    DbWriter.name(initialDatabaseUrl))

  /** also watch the dbWriter for death **/
  /** note you can also just import the context._ **/
  context.watch(dbWriter)

  /**
   * The LogProcessor will receive a message indicating a new file has arrived.
   * The FIle will be read and transformed into a Vector of DBWriter.Line objects.
   * Each line will be sent to its DbWriter one at a time.  The Terminated message
   * appears to indicate a database issue in which case a new DbWriter will replace
   * the failed DbWriter if ther are alternate database urls available. Otherwise the
   * log procesor will exit by sending itself a PoisonPill
   *
   */
  import LogProcessor._
  import DbWriter._
  def receive = {
    case LogFile(file) =>
      emit(myName + " LogFile(%s)".format(file),true)
      val lines: Vector[DbWriter.Line] = parse(file)
      lines.foreach(dbWriter ! _)
      dbWriter ! EndOfRecord
    case Terminated(_) =>
      if (alternateDatabases.nonEmpty) {
        emit(myName + " Terminated Message, NonEmpty Url",true)
        val newDatabaseUrl = alternateDatabases.head
        alternateDatabases = alternateDatabases.tail
        dbWriter = context.actorOf(
          DbWriter.props(newDatabaseUrl),
          DbWriter.name(newDatabaseUrl))
        /** register this thing to be watched **/
        context.watch(dbWriter)
      } else {
        emit(myName + " All Db nodes broken, stopping.",true)
        self ! PoisonPill
      }
  }

  def myName = "LogProcessor"
}

/** trait log parsing using dummy line for now **/
trait LogParsing extends ActorLogging {
  self: Actor =>
  import DbWriter._
  // Parses log files. creates line objects from lines in the log file
  // if the file is corrupt a CorruptedFIleException is thrown
  /**
   * This will only work for medium size files ..
   * NOT HUGE ONES
   * case class Line(time: Long, message: String, messageType: String)
   *
   */
  def parse(file: File): Vector[Line] = {
    emit("LogParsing --> parse opening file and converting to Vector[Line]",true)
    // implement parser here , now just return dummy value
    //     Source.fromFile( file ).getLines.foreach { line =>
    //         println(line)
    //      }

    val v = for (vec <- Source.fromFile(file).getLines) yield {
      emit("LogParsing --> parse reading:" + vec,false)
      Line(System.currentTimeMillis(), vec, "text")
    }
    /** TBD this is HUGELY inefficeint .. so just for testing we leave it **/
    v.toVector
  }
}