package com.goticks.book.examples


import com.typesafe.config._
import akka.actor._

import com.goticks.utilities.Utils._
/**
 * This example is the front end. It will acquire a 
 * pointer to the address of the backend Actor using
 * actorSelection. Then it will send a message to the 
 * back end actor.
 * 
 */
object Example65 extends App {
  
  def startFrontEndServer = {
  println("frontend server running")
  val conf = """
  akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
      enabled-transports = ["akka.remote.netty.tcp"]
      netty.tcp{
        hostname = "0.0.0.0"
        port = 2553
      }
    }
  }
  """
  emit("getting local configuration")
  val config = ConfigFactory.parseString(conf)
  emit("Creating an ActorSystem Object")
  ActorSystem("frontend", config)
  }
  
  val frontend = startFrontEndServer
  
  /** get reference to the Simple actor in backend system **/
  
  /**
   * create a String path to the back end actor called 
   * simple:
   * akka.tcp = protocol
   * backend  = actor system
   * 0.0.0.0  = port
   * user = guardiean
   * simple = name of actor 
   */
  val path = "akka.tcp://backend@0.0.0.0:2552/user/simple"
  emit(s"setup path to back end : $path")
  
  /** create an ActorSelection object representing the 
   *  back end simple actor 
   */
  val simple = frontend.actorSelection(path)
  emit(s"getting actorRef to Simple $simple")
  emit("sending simple message")
  
  /**
   * Send a message to the backend actor system 
   */
  simple ! "Hello Remote World!"
  emit("running at end")
  
}