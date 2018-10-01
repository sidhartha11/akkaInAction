package zzz.akka.avionics

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, Props, ReceiveTimeout, Terminated}
import scala.concurrent.duration._
import akka.util.Timeout

object Pilots {
  
  case object ReadyToGo
  case object RelinquishControl
  
}

class Pilot extends Actor {
  import Pilots._
  import Plane._
  
  implicit val timeout = Timeout(1 second)
  var controls: ActorRef = context.system.deadLetters
  var copilot:  ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  
  val copilotName = context.system.settings.config.getString(
      "zzz.akka.avionics.flightcrew.copilotName")
  val copilotPath = "../" + copilotName
  val autopilotPath = "../" + autopilot
    
   override def receive = {
   
    case ReadyToGo => 
      /** this message is being sent to this actor's parent **/
      context.parent ! GiveMeControl 
      val fcopilot = context.actorSelection(copilotPath)
      fcopilot  ! Identify(copilotPath)
      val fautopilot = context.actorSelection(autopilotPath)
      fautopilot ! Identify(autopilotPath)
      
    case ActorIdentity(`copilotPath`, Some(actor)) =>
      copilot = actor
      println(s"ActorIdentity was received from ${actor.path}")
      
    case ActorIdentity(`autopilotPath`, Some(actor)) =>
      context.watch(actor)
      autopilot = actor
      println(s"ActorIdentity was received from ${actor.path}")
      
    case ActorIdentity(`copilotPath`, None) => 
      println(s"Remote actor not available $copilotPath")
      
    case ActorIdentity(`autopilotPath`, None) => 
      println(s"Remote actor not available $autopilotPath")
    case Controls(controlSurfaces) => 
      controls = controlSurfaces
  }
}