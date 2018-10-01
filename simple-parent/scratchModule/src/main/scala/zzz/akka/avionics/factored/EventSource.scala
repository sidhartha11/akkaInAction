package zzz.akka.avionics.factored

import akka.actor.{Actor, ActorRef}

/**
 * Note the form here: Author creates an Object containing 
 * the types of messages used for a registration process
 * and then proceeds to cretae a trait of the same name.
 * So we have a companion Object and Trait. 
 * Note This is a refactored EventSource Trait. The original 
 * EventSource now just declares it two functions:
 * sendEvent[T] and eventSourceReceive.
 * 
 */
object EventSource {
  
  // Messages used by listeners to register and unregister
  // themselves
  case class RegisterListener(listener: ActorRef)
  case class UnregisterListener(listener: ActorRef)
}

trait EventSource {
  def sendEvent[T](event: T): Unit
  def eventSourceReceive: Actor.Receive
}

/** the only change here is the self-typing **/
/** means that this class can only be extended by a class that extends Actor **/
trait ProductionEventSource extends  EventSource { 
  this: Actor => 
    import EventSource._
    // We're going to use a Vector but many structures wuld be 
    // adequate
    var listeners = Vector.empty[ActorRef]
    
    // Sends the event to all of our listeners
    def sendEvent[T](event: T): Unit = listeners foreach {
      _ ! event 
    }
    
    // We create a specific partial function to handle the 
    // messages for our event listener. Anything that mixes in 
    // our trait will need to compose this receiver
    // :+ appends an element to the Vector
    def eventSourceReceive: Receive = {
      case RegisterListener(listener) => 
        listeners = listeners :+ listener 
      case UnregisterListener(listener)  =>
        listeners = listeners filter {
          _ != listener
        }
    }
}