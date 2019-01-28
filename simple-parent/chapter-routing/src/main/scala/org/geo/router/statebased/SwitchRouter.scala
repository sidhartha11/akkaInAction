package org.geo.router.statebased

import akka.actor.{ActorRef , ActorLogging}
import akka.actor.AbstractActor.Receive
import akka.actor.Actor

case class RouteStateOn()
case class RouteStateOff()

class SwitchRouter(normalFlow: ActorRef , cleanUp: ActorRef)
extends Actor with ActorLogging  {
  def on: Receive = {
    case RouteStateOn =>
      log.warning("Received on while already in on state")
      
    case RouteStateOff => context.become(off)
    case msg: AnyRef => {
      log.info("got a msg while in on state, sending to normalFlow {}",msg)
      normalFlow ! msg 
    }
  }
  
  def off: Receive = {
    case RouteStateOn => 
      log.info("got an RouteStateOn while in Off state")
      context.become(on)
    case RouteStateOff => 
      log.warning("Received off while already in off state")
    case msg: AnyRef => {
      log.info("got a message in off state, cleaning up {}",msg)
      cleanUp ! msg 
    }
    }
   def receive = off     // actor starts with off state 
  }