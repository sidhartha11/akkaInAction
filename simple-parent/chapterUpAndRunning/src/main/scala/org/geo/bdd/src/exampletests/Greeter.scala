package org.geo.bdd.src.exampletests

import akka.actor.{Actor,ActorLogging}

/** Greeting Actor takes only one message:Greeting **/
case class Greeting(message: String)
class Greeter extends Actor with ActorLogging  {
  def receive = {
    case Greeting(message) => log.info("Hello {}!", message)
  }
}