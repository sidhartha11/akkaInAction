package zzz.akka.avionics

import akka.actor.Actor
import scala.concurrent.duration._

// THis trait allows us to create different Flight Attendants
// with different levels of responsiveness

trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = 
    scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkname: String)
  case class Drink(drinkname: String)
  
  // By default we will make attendants that respond
  // within 5 minutes
  
  def apply() = new FlightAttendant 
                with AttendantResponsiveness {
    val maxResponseTimeMS = 300000
  }
}

/**
 * you cannot extend FlightAttendant without also 
 * extending AttendantRepsonsiveness 
 */
class FlightAttendant extends Actor {
  this: AttendantResponsiveness =>
  /** import all the messages from FlightAttendant Object **/
  import FlightAttendant._
  // bring the execution context into implicit scope for the 
  // scheduler below
  // context is inherited from Actor
  implicit val ec = context.dispatcher 
  
  def receive = {
    case GetDrink(drinkname) => 
      // We don't respond right away, but use the scheduler to 
      // ensure we do eventually 
      // Note, here the author does not first save sender
      // Possibly because this is not a closure 
      // The second parameter of scheduleOnce takes a proper 
      // ActorRef, not a by-name parameter, 
      // so the sender is frozen at the point of scheduling
      context.system.scheduler.scheduleOnce(
          responseDuration,sender, Drink(drinkname))
  }
}