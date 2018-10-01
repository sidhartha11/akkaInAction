package zzz.akka.avionics

import akka.actor.{Actor}
import scala.concurrent.duration._
import akka.actor.ActorInitializationException
import akka.actor.ActorKilledException
import akka.actor.SupervisorStrategy._


object IsolatedLifeCycleSupervisor {
  
  // Messages we use in case we want people to be
  // able to wait for us to finish starting
  case object WaitForStart
  case object Started
}

trait IsolatedLifeCycleSupervisor extends Actor {
  import IsolatedLifeCycleSupervisor._
  
  def receive = {
    // Signify that we've started
    case WaitForStart =>
      sender ! Started 
      // We don't handle anything else, but we gve a decent
      // error messager starting the error
    case m => 
      throw new Exception(
          s"Don't call ${self.path.name} directly ($m).")
  }
  
  // to be implemented byu subclass
  def childStarter() : Unit
  
  // Only start the children when we're started
  final override def preStart() { childStarter() }
  
  // Don't call preStart(), which would be the 
  // default behaviour 
  final override def postRestart(readon: Throwable) { }
  
  // Don't stop the children, which would be the 
  // default behaviour 
  final override def preRestart(reason: Throwable,
                                message: Option[Any]) {}
}

/**
 * @author george
 * <pre>
 * Note the self type below:
 * this: SupervisionStrategyFactory =>
 * This means that you cannot extend this class without also
 * mixing in and/or extending the SupervisionStrategyFactory
 * </pre>
 */
abstract class IsolatedResumeSupervisor ( 
    maxNrRetries: Int = -1,
    withinTimeRange: Duration = Duration.Inf
    ) 
    extends IsolatedLifeCycleSupervisor {
      this: SupervisionStrategyFactory => 
    
    /**
     * Type InitializationStrategy contains all the required
     * constants needed below.
     */
    override val supervisorStrategy = makeStrategy (
        maxNrRetries, withinTimeRange ) {
      case _: ActorInitializationException => Stop 
      case _: ActorKilledException => Stop 
      case _: Exception => Resume
      case _ => Escalate
      
    }
}

abstract class IsolatedStopSupervisor (
    maxNrRetries: Int = -1 , 
    withinTimeRange: Duration = Duration.Inf)
    extends IsolatedLifeCycleSupervisor {
  this: SupervisionStrategyFactory => 
    
    override val supervisorStrategy = makeStrategy (
        maxNrRetries, withinTimeRange ) {
      case _: ActorInitializationException => Stop
      case _: ActorKilledException => Stop
      case _: Exception => Stop
      case _ => Escalate
    }
}