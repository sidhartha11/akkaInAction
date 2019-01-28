package org.geo.channels.eventstream

import akka.actor.{Actor, ActorRef, ActorLogging}
/**
 * To create an Actor you need to 
 * extend the Actor trait and override
 * the receive function: Receive
 */
class EchoActor extends Actor with ActorLogging  {
  
  /**
   * This actor will only process messages that are of 
   * type: ActorRef 
   */
  def receive: Receive  = {
    case msg: ActorRef => 
      log.info("got an ActorRef message {}",msg)
      sender() ! msg 
  }
}