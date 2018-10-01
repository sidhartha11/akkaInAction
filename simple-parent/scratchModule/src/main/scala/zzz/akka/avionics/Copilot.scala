package zzz.akka.avionics

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, Props, ReceiveTimeout, Terminated}
/**
 * Actors are created by extending the Actor 
 * Trait.
 */
class Copilot extends Actor {
  import Pilots._
  /**
   * Author initializes variable actorRefs with 
   * context.system.deadLetters
   */
  var controls: ActorRef = context.system.deadLetters
  var pilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  
  val pilotName = context.system.settings.config.getString(
      "zzz.akka.avionics.flightcrew.pilotName")
      
  val spilotName = "../" + pilotName
  val sautoPilotName = "../Autopilot" 
      
  override def receive = {
    case ReadyToGo => 
      /**
       * This section will send identity messages that will 
       * return the associated ActorRef
       */
      val fpilot = context.actorSelection(spilotName)
      fpilot  ! Identify(spilotName)
      val fautopilot = context.actorSelection(sautoPilotName)
      fautopilot ! Identify(sautoPilotName)
      
      /** retrieve the pilot and copilot ActorRefs **/
    case ActorIdentity(`spilotName`, Some(actor)) =>
      pilot = actor
      println(s"ActorIdentity was received from ${actor.path}")
      
    case ActorIdentity(`sautoPilotName`, Some(actor)) =>
      context.watch(actor)
      autopilot = actor
      println(s"ActorIdentity was received from ${actor.path}")
      
    case ActorIdentity(`spilotName`, None) => 
      println(s"Remote actor not available $spilotName")
      
    case ActorIdentity(`sautoPilotName`, None) => 
      println(s"Remote actor not available $sautoPilotName")
  }
  
}