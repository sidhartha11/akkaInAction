package org.geo.akkaconcurrency.chapter7

import akka.actor.{ActorSystem, Actor,Props}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global





object Chapter7 {
  /**
   * To run a Scala Application, you need to 
   * implement the main function or 
   * extend the App trait.
   */
  def main(args: Array[String]) {
    checkExistence 
  }
  
   def checkExistence = {
        /**
     * Create a root for all the child actors to exist in
     */
    val system = ActorSystem("ThSystem")
    
    /**
     * Create an anonomous actor to live in the root
     * Note: each actor within a ActorSystem must have a unique name
     */
    val a = system.actorOf (
        Props(
            new Actor {   
              println("checking existence") 
              val doesNotExist = context.actorSelection("zzz.akka.avionics.flightcrew.leadAttendantName")
              println("calling foreach on the resolveOne function")
              doesNotExist.resolveOne(1 second).foreach(p => println(p))
              //println("doesNotExist=%s".format(doesNotExist))
              def receive = Actor.emptyBehavior 
            }
            ) , 
            "AnonymousActor"
            )
    // prints the path of this actor
    println(a.path)
    // printls the name of the actor
    println(a.path.name)
        // prints the path of this actor
  }
  /** display actor path **/
  def displayActorPath = {
        /**
     * Create a root for all the child actors to exist in
     */
    val system = ActorSystem("ThSystem")
    
    /**
     * Create an anonomous actor to live in the root
     * Note: each actor within a ActorSystem must have a unique name
     */
    val a = system.actorOf (
        Props(
            new Actor {
              def receive = Actor.emptyBehavior 
            }
            ) , 
            "AnonymousActor"
            )
    val b = system.actorOf (
        Props(
            new Actor {
              def receive = Actor.emptyBehavior 
            }
            ) , 
            "AnonymousActor2"
            )
    // prints the path of this actor
    println(a.path)
    // printls the name of the actor
    println(a.path.name)
        // prints the path of this actor
    println(b.path)
    // printls the name of the actor
    println(b.path.name)
  }
}