package zzz.akka.avionics

// Imports to help us create Actors, plus logging
import akka.actor.{Props,Actor,ActorSystem,ActorLogging}
// The duration package objet extends Ints with some
// timing functionality 
import scala.concurrent.duration._

/**
 * The actual Altimeter actor class
 * Things to note:
 * The variables declared all instantiated at construction
 * time. This means the 'var' variables cannot be touched any
 * where except in the recieve method.
 */
class Altimeter extends Actor with ActorLogging 
                              with EventSource {
  /**
   * One of those funny scala needs to import the object
   * into this class
   * 
   */
  import Altimeter._
  // We need an "ExecutionContext" for the scheduler. This
  // Actor's dispatcher can serve that purpose. The
  // scheduler's work will be dispatched on this Actor's own
  // dispatcher
  implicit val ec = context.dispatcher
  
  // The maximum ceiling of our plane in 'feet'
  val ceiling = 43000
  
  // The maximum rate of climb for our plane in 
  // 'feet per minute' 
  val maxRateOfClimb = 5000
  
  // The varying rate of climb depending on the movement of
  // the stick
  var rateOfClimb = 0f
  
  // Our current altitude
  var altitude = 0d
  
  // As time passes, we need to change the altitude based on 
  // the time passed. The lastTick allows us to figure out
  // how much time has passed
  var lastTick = System.currentTimeMillis
  
  // We need to periodically update our altitude. This
  // scheduled message send will tell us when to do that 
  val ticker = context.system.scheduler.schedule(
      100.millis, 100.millis, self, Tick)
      
  // An internal message we send to ourselves to tell us 
  // to update our altitude
  case object Tick 
  
  def altimeterReceive: Receive = {
    // Our rate of climb has changed
    case RateChange(amount) =>
      // Truncate the range of 'amount' to [-1, 1]
      // before multiplying
      rateOfClimb = amount.min(1.0f).max(-1.0f) *
      maxRateOfClimb 
      
      log info(s"Altimeter changed rate of climb to $rateOfClimb.")
      
    case Tick =>
      val tick = System.currentTimeMillis
      altitude = altitude + ((tick - lastTick) / 60000.0) * rateOfClimb
      lastTick = tick
      /** 
       *  send a AltitudeUpdate message to all registered listeners
       */
      sendEvent(AltitudeUpdate(altitude))
  }
  
  /**
   * Interesting way of combining two partial function.
   */
  def receive = eventSourceReceive orElse altimeterReceive
  
  // Kill our ticker when we stop
  override def postStop(): Unit = ticker.cancel()
}

/**
 * Author uses this type of structure for actors:
 * Creates a companion object that contains the functionality
 * associated with this actor. It is possible that in an Enterprise
 * environment, this approach would not be scalable. Since the message
 * is tightly coupled to the actor ... maybe ... we will see.
 */
object Altimeter {
  // Sent to the Altimeter to inform it about 
  // rate-of-climb changes 
  case class RateChange(amount: Float)
  
  // Sent by the Altimeter at regular intervals
  case class AltitudeUpdate(altitude: Double)
}