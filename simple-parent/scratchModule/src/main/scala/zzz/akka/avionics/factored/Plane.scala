package zzz.akka.avionics.factored

import akka.actor.{Props,Actor,ActorLogging}
import zzz.akka.avionics.ControlSurfaces

object Plane {
  // Returns the control surface to the Actor that 
  // asks for them
  case object GiveMeControl
}

// We want the plane to won the Altimeter and we're going to 
// do that by passing in a specific factory we can use to 
// build the Altimeter

class Plane extends Actor with ActorLogging  {
  import zzz.akka.avionics.factored.Altimeter._
  import zzz.akka.avionics.factored.Plane._ 

  
  /**
   * This version of actorOf ties the altimeter actor 
   * to the plane Actor as a child.
   */
  val altimeter = context.actorOf (
      Props(Altimeter()), "Altimeter")
      
  val controls = context.actorOf(
      Props(new ControlSurfaces(altimeter)),"ControlSurfaces")
      
  def receive = {
    case AltitudeUpdate(altitude) => 
      log info(s"Altitude is now: $altitude")
      
    case GiveMeControl => 
      log info ("Plane giving control.")
      sender ! controls 
  }
  
  import zzz.akka.avionics.factored.EventSource._
  override def preStart() {
    altimeter ! RegisterListener(self)
  }
}