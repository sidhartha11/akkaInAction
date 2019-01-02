package aia.deploy

/**
 * Import required traits for creating an Actor
 * 
 */
import LoggerMessages._

import akka.actor.{ Actor, Props, ActorSystem }
import com.typesafe.config.ConfigFactory
import akka.event.Logging

class SimpleLogger2 extends Actor
 {
  val log = Logging(context.system,"coollogger")
//  val log = Logging(context.system,this)

  def receive = {
    case TICKER(msg) => 
      log.info(msg)
    case e =>
      log.error("unknown message " + e)
  }
}
object TestLogger2 extends App {
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
  val system = ActorSystem("TestLogger2",config)
  /**
   * create an actor
   */
  val actor = system.actorOf(Props[SimpleLogger2])
//  val config = system.settings.config
  /**
   * send a test tell message to the newly created actor with
   * the logging configured
   */
  actor ! TICKER("I am ticking")
  actor ! BADMESSAGE("I am not ticking")
  
}