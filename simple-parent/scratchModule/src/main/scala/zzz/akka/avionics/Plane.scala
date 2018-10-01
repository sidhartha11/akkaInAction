package zzz.akka.avionics

import akka.actor.{Props,Actor,ActorLogging,ActorRef}

object Plane {
  // Returns the control surface to the Actor that 
  // asks for them
  case object GiveMeControl
  // How we respond to the GiveMeControl message
  case class Controls(controls: ActorRef)
}

// We want the plane to won the Altimeter and we're going to 
// do that by passing in a specific factory we can use to 
// build the Altimeter

class Plane extends Actor with ActorLogging  {
  import Altimeter._
  import Plane._ 

  
  /**
   * This version of actorOf ties the altimeter actor 
   * to the plane Actor as a child.
   */
  val altimeter = context.actorOf (
      Props[Altimeter], "Altimeter")
      
  val controls = context.actorOf(
      Props(new ControlSurfaces(altimeter)),"ControlSurfaces")
  
  // Construction of the plane 
      
  val cfgstr = "zzz.akka.avionics.flightcrew"
  /** get the config settings **/
  val config = context.system.settings.config
  /** get the Pilot instantiated **/
  val pilot = 
    context.actorOf(Props[Pilot], config.getString(s"$cfgstr.pilotName"))
  
  /** get the copilot instantiated **/
  val copilot = context.actorOf(Props[Copilot],
      config.getString(s"$cfgstr.copilotName"))
      
  /** get the autopilot instantiated **/
  val autopilot = context.actorOf(
      Props[Autopilot],"AutoPilot")
  
  /** get the flightAttendant instantiated **/
  val flightAttendant = context.actorOf(
      Props(LeadFlightAttendant()),
      config.getString(s"$cfgstr.leadAttendantName"))
      
  def receive = {
    case AltitudeUpdate(altitude) => 
      log info(s"Altitude is now: $altitude")
      
    case GiveMeControl => 
      log info ("Plane giving control.")
      sender ! Controls(controls)
  }
  
  import EventSource._
  override def preStart() {
    altimeter ! RegisterListener(self)
    
    List(pilot, copilot) foreach {
      _ ! Pilots.ReadyToGo
    }
  }
}