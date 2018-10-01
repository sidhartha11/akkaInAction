package zzz.akka.avionics

import akka.actor.{Actor, ActorRef, Props}

// The Lead is going to construct its own subordinates.
// We'll haved a policy to vary that 
trait AttendantCreationPolicy {
  // Feel free to make this configurable!
  val numberOfAttendants: Int = 8 
  def createAttendant: Actor = FlightAttendant()
}

// We'll also provide a mechanism for altering how we create
// the LeadFlightAttndant 
trait LeadFlightAttendantProvider {
  def newLeadFlightAttendant: Actor = LeadFlightAttendant()
}

object LeadFlightAttendant {
  case object GetFlightAttendant 
  case class Attendant(a: ActorRef)
  def apply() = new LeadFlightAttendant 
                with AttendantCreationPolicy
}

/**
 * Must extend AttendantCreationPolicy 
 */
class LeadFlightAttendant extends Actor {
  this: AttendantCreationPolicy => 
  import LeadFlightAttendant._
  // After we've successfully spooled up the 
  // LeadFlightAttendant, we're going to have it create all of 
  // its subordinates 
  
  override def preStart() {
    import scala.collection.JavaConverters._
    val attendantNames = 
      context.system.settings.config.getStringList(
          "zzz.akka.avionics.flightcrew.attendantNames").asScala
          
    attendantNames take numberOfAttendants foreach {
      name => 
        // we create the actors within our context such that 
        // they are childen of this actor 
        context.actorOf(Props(createAttendant), name)
    }
  }
  
  // 'children' is an Iterable. This method returns 
  // a random one 
  // take an random number of elements and return ther last one
  def randomAttendant(): ActorRef = {
    context.children.take( 
        scala.util.Random.nextInt(numberOfAttendants) + 1).last
  }
  
  def receive = {
    /** sent a message wishing for an Attendant **/
    case GetFlightAttendant => 
      sender ! Attendant(randomAttendant())
    /** this is a request, just forward it to a random attendant **/
    case m => 
      randomAttendant() forward m 
  }
   
}