package org.geo.utilities.logapplication2

import akka.actor.{Props,Actor,ActorLogging,PoisonPill}
import org.geo.utilities.Geoutils._

object DbWriter {
  
  def props(databaseUrl: String) = 
    Props(new DbWriter(databaseUrl))
    
  def name(databaseUrl: String) = 
    s"""db-writer-${databaseUrl.split("/").last}"""
  
  case class Line(time: Long, message: String, messageType: String)
  /**
   * Adding an additional message type here to signify end of records to be processed.
   */
  case object EndOfRecord
  
}

class DbWriter(databaseUrl: String) extends Actor with ActorLogging  {
  val connection = new DbCon(databaseUrl)
  emit(myName + " Constructor",true)
  import DbWriter._
  def receive = {
    case Line(time, message, messageType) => 
      emit(myName + " Line(%s,%s,%s)".format(time,message,messageType),false)
      /**
       * This section can be used to simulate a node down error.
       */
      connection.write(Map('time -> time, 'message -> message,
          'messageType -> messageType))
    case EndOfRecord => 
      emit(myName + " EndOfRecord, sending self PoisonPill ",true)
      self ! PoisonPill 
  }
  
  override def postStop(): Unit = {
    emit(myName + " postStop called",true)
    // connection.close()
  }
  
  def myName = "DbWriter"
}