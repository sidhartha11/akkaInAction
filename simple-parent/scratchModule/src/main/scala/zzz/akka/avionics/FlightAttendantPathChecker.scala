package zzz.akka.avionics

import akka.actor.{Props}
object FlightAttendantPathChecker {
  
  /** main method **/
  def main(args: Array[String]) {
    /** top of the hierarchy (ActorSystem(PlaneSimulation)) **/
    val system = akka.actor.ActorSystem("PlaneSimulation")
    
    val lead = system.actorOf(Props(
        new LeadFlightAttendant with AttendantCreationPolicy),
        system.settings.config.getString(
            "zzz.akka.avionics.flightcrew.leadAttendantName"))
            
    Thread.sleep(2000)
    system.terminate()
        
  }
}