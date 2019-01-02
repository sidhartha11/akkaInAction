package org.geo.bdd.src.exampletests

import akka.actor.{Actor,ActorLogging,ActorRef,Props}

object Greeter02 {
  def props(listener: Option[ActorRef] = None) = 
    Props(new Greeter02(listener))
}
class Greeter02 (listener: Option[ActorRef])
extends Actor with ActorLogging {
  def receive = {
    case Greeting(who) =>
      val message = "Hello " + who + "!" 
      log.info(message)
      listener.foreach(_ ! message )
  }
}