package aia.deploy

/**
 * Import required traits for creating an Actor
 * 
 */
case class TICKER(msg: String)
case class BADMESSAGE(msg: String)

import akka.actor.{ Actor, Props, ActorSystem,ActorLogging }
import com.typesafe.config.ConfigFactory

class SimpleLogger extends Actor
with ActorLogging {
  def receive = {
    case TICKER(msg) => 
      log.info(msg)
    case e =>
      log.error("unknown message {}" , e)
  }
}
object TestLogger extends App {
  /**
   * Create the required objects for starting the actor
   * 
   */
  /**
   * create a configuration object
   */
  val config = ConfigFactory.load("loggingTest.conf").resolve()
  /**
   * create an ActorSystem, passing the config object
   */
  val system = ActorSystem("testlogger",config)
  /**
   * create an actor
   */
  val actor = system.actorOf(Props[SimpleLogger])
//  val config = system.settings.config
  /**
   * send a test tell message to the newly created actor with
   * the logging configured
   */
  actor ! TICKER("I am ticking")
  actor ! BADMESSAGE("I am not ticking")
  
}