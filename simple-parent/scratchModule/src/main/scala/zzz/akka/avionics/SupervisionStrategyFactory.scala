package zzz.akka.avionics

import akka.actor.{SupervisorStrategy,OneForOneStrategy,
  AllForOneStrategy}
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration.Duration

/**
 * <pre>
 * Authors way of added a type of object-orientedness
 * to his application. We have one wide open trait that is 
 * futher refined by subtraits specializing in the type of 
 * supervisor strategy to be employed. 
 */
trait SupervisionStrategyFactory {
  
  def makeStrategy (
      maxNrRetries: Int , 
      withinTimeRange: Duration
      )(decider: Decider): SupervisorStrategy
  
}

trait OneForOneStrategyFactory extends
SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int,
      withinTimeRange: Duration )
  (decider: Decider): SupervisorStrategy = 
    OneForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}

trait AllForOneStrategyFactor 
extends SupervisionStrategyFactory {
  def makeStrategy (
  maxNrRetries: Int, 
  withinTimeRange: Duration 
)(decider: Decider): SupervisorStrategy = 
  AllForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}