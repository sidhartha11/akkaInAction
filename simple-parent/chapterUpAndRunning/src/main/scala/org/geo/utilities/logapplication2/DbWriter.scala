package org.geo.utilities.logapplication2

import akka.actor.{Props,Actor,ActorLogging}
object DbWriter {
  
  def props(databaseUrl: String) = 
    Props(new DbWriter(databaseUrl))
    
  def name(databaseUrl: String) = 
    s"""db-writer-${databaseUrl.split("/").last}"""
  
  case class Line(time: Long, message: String, messageType: String)
  
}

class DbWriter(databaseUrl: String) extends Actor with ActorLogging  {
  val connection = new DbCon(databaseUrl)
  log.info(myName + " Constructor")
  import DbWriter._
  def receive = {
    case Line(time, message, messageType) => 
      log.info(myName + " Line(%s,%s,%s)".format(time,message,messageType))
      connection.write(Map('time -> time, 'message -> message,
          'messageType -> messageType))
  }
  
  override def postStop(): Unit = {
    log.info(myName + " postStop called")
    connection.close()
  }
  
  def myName = "DbWriter"
}