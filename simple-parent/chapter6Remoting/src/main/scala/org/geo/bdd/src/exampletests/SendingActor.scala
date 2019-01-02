package org.geo.bdd.src.exampletests

import akka.actor.{Actor,Props,ActorRef}
object SendingActor {
  /** repository of messages processed by the SendingActor **/
  /** set up the properties of an actor that takes a ActorRef parameter **/
  def props(receiver: ActorRef) = 
    /** Props take a by-name parameter, so the new ... is not executed until needed **/
    Props(new SendingActor(receiver))
    
    case class Event(id: Long)
    case class SortEvents(unsorted: Vector[Event])
    case class SortedEvents(sorted: Vector[Event])
}
class SendingActor(receiver: ActorRef) extends Actor {
  
  import SendingActor._
  
  /**
   * The receive processes only one message, SortEvents.
   * It send the sorted events to the ActorRef that was passed in 
   * via constructor parameter. 
   */
  def receive = {
    case SortEvents(unsorted) => 
        receiver ! SortedEvents(unsorted.sortBy(_.id))
  }
}