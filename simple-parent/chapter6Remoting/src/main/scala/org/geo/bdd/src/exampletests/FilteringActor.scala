package org.geo.bdd.src.exampletests

/** import actor classes,traits **/
import akka.actor.{Actor,Props,ActorRef}

/** create a companion object to contain messages **/
object FilteringActor {
  def props(nextActor: ActorRef, bufferSize: Int) =
    Props(new FilteringActor(nextActor,bufferSize))
    
  case class Event(id: Long)
}
class FilteringActor(nextActor: ActorRef, bufferSize: Int)  extends Actor {
  /** import the messages that are processed **/
  import FilteringActor._
  /** create an immutable Vector to contain events **/
  var lastMessages = Vector[Event]()
  /** implement the receive function to process messages **/
  def receive = {
    /** message of type Event received **/
    /** if not a duplicate, add it to the end of the Vector **/
    case msg: Event =>
      if (!lastMessages.contains(msg) ) {
        lastMessages = lastMessages :+ msg
        /** send this msg back to the input ActorRef supplied at construction **/
        nextActor ! msg
        
        if (lastMessages.size > bufferSize) {
          /** discard the oldest message **/
          lastMessages = lastMessages.tail
        }
      }
  }
}