package zzz.akka.avionics

import akka.actor.SupervisorStrategyConfigurator
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy

/**
 * @author george
 * <pre>
 * This is for actors that are supervised by the 
 * user guardian via system.actorOf
 * If we create a child of the user guardian via system.actorOf, 
 * what supervisor strategy gets instantiated? 
 * Well, by default, it's the default, which shouldn't be 
 * much of a surprise. That means that if the user guardian's 
 * children throw an exception, they're going to restart. 
 * If that's not what you want, you can modify it by a change 
 * to the configuration.
 *
 */
class UserGuardianStrategyConfigurator 
extends SupervisorStrategyConfigurator {
  def create(): SupervisorStrategy = {
    OneForOneStrategy() {
      case _ => {
        println(">>>>> resuming strategy called")
        Resume
      }
    }
  }
}