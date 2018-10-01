package zzz.akka.avionics.factored

import akka.actor.{Props, Actor , ActorRef, ActorSystem}
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

// The futures created by the ask syntax need an 
// execution contest on which to run, and we will use the 
// default global instance for that context 

import scala.concurrent.ExecutionContext.Implicits.global
import zzz.akka.avionics.ControlSurfaces

object Avionics {
  
  // needed for '?' below
  implicit val timeout = Timeout(5 seconds)
  /**
   * Creates a system of actors used to create child actors
   */
  val system = ActorSystem("PlaneSimulation")
  /** create a actorRef to the Plane object **/
  val plane = system.actorOf(Props[Plane], "Plane")
  
  /** main routine used to test various actors in the avionics system **/
  def main(args: Array[String]) {
    // Grab the controls
    
    /** 
     *  wait for the future to be satisfied, 5 secs 
     *  Generally waiting for an Actor to finish is
     *  considered an anti pattern. The mapTo coerces
     *  the returned type of "Any" to an ActorRef.
     */
    
    val control = Await.result(
        (plane ? Plane.GiveMeControl).mapTo[ActorRef],
        5 seconds)
        
    // takeOff!
    // cause the ControlSurfaces.StickBack message to be sent
    system.scheduler.scheduleOnce(200.millis) {
      control ! ControlSurfaces.StickBack(1f)
    }
    
    // level out 
    system.scheduler.scheduleOnce(1 seconds) {
      control ! ControlSurfaces.StickBack(0f)
    }
    
    // Climb
    system.scheduler.scheduleOnce(3 seconds) {
      control ! ControlSurfaces.StickBack(0.5f)
    }
    
    // Level out
    system.scheduler.scheduleOnce(4 seconds ) {
      control ! ControlSurfaces.StickBack(0f)
    }
    
    // Shut down
    system.scheduler.scheduleOnce(5.seconds) {
      // system.shutdown()
      system.terminate()
    }
  }
}