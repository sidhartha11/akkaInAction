package aia.deploy

import akka.event.Logging.{Error,Warning,Info,Debug,LoggerInitialized,InitializeLogger}
import akka.actor.{Actor,ActorLogging}

class EventHandlerExample extends Actor 
with ActorLogging{
  def receive = {
    case InitializeLogger(_) =>
      sender ! LoggerInitialized 
      
    case Error(cause,logSource,logClass,message) =>
      log.error(logSource + "[ERROR] {}" , message)
      
    case Warning(logSource,logClass,message) =>
      println("WARN " + message)
      
    case Info(logSource, logClass, message) => 
      println(logSource + "[INFO]" + message)
      
    case Debug(logSource, logClass, message) =>
      println("DEBUG " + message)
  }
  
}