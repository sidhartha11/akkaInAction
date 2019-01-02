package com.goticks.book.examples

import com.typesafe.config._
import akka.actor._

import com.goticks.utilities.Utils._
/**
 * Simple actor creation ..
 * Basically here we create a class that extends the Actor trait
 * This allows this class to become an Actor 
 * By mixing in ActorLogging we get access to a logger
 */
class Simple extends Actor with ActorLogging {
  log.info("actor is being instantiated:%s".format(this.getClass))
  def receive = {
    
    case m => log.info(s"received $m!")
  }
  
}

object Example63 extends App {
  
  /**
   * create a simple backend server by manually reading in the configuration 
   * via a simple string that defines a valid configuration.
   * Here this configuation simply says to enable akka remoting module.
   * defines a hostname, here local, and a port number to listen to
   */
  def startBackEndServer: ActorSystem = {
  val conf = """
  akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
      enabled-transports = ["akka.remote.netty.tcp"]
      netty.tcp{
        hostname = "0.0.0.0"
        port = 2552
      }
    }
  }
  """
  /**
   * Here create a Config object that is used to store the Actor's configuration
   * ConfigFactory is used to read in the conf String. NOrmally this would be a
   * file on the class path. 
   */
  val config = ConfigFactory.parseString(conf)
  /**
   * Here this line will create an actor system
   */
  val actorSystem = ActorSystem("backend", config)
  actorSystem
  }
  
  /** create the ActorSystem object which can be used to create an ActorRef **/
  val backend = startBackEndServer
  
  /** create an ActorRef from the ActorSystem; this will in essence start the actor **/
  emit("running Simple actor in backEnd")
  val backendActorRef = backend.actorOf(Props[Simple],"simple")
  emit("running at end")
}