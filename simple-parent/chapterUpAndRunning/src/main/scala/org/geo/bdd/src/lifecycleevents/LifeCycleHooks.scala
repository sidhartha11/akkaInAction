package org.geo.bdd.src.lifecycleevents
import akka.actor.{Actor,ActorLogging,Props,ActorSystem}
class LifeCycleHooks extends Actor
with ActorLogging {
  log.info("Constructor")
  
  override def preStart(): Unit = {
    log.info("preStart")
  }
  
  override def postStop(): Unit = {
    log.info("postStop")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("preRestart: reason=%s, message=%s".format(reason,message))
    super.preRestart(reason,message)
  }
  
  override def postRestart(reason: Throwable): Unit = {
    log.info("postRestart: reason=%s".format(reason))
    super.postRestart(reason)
  }
  
  def receive = {
    case "restart" => 
      throw new IllegalStateException("force restart")
    case msg: AnyRef => 
      log.info("Receive: msg=%s".format(msg))
      sender() ! msg
  }
}