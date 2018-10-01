package zzz.akka.avionics

import akka.actor.{Actor}
trait PilotProvider {
 
  def newPilot: Actor = new Pilot
  def newCopilot: Actor = new Copilot
  def newAutopilot: Actor = new Autopilot
}