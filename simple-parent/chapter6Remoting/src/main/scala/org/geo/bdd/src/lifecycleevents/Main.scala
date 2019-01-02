package org.geo.bdd.src.lifecycleevents

/**
 * In order for a main to create a top-level actor that is
 * supervised by the /user guardian supervisor, you 
 * must import ActorSystem
 */
import akka.actor.{ActorSystem, Props}

object Main {
  /**
   * To create an actor system from which to create
   * hierarchical system of Actors you need a 
   * ActorSystem object
   */
  def main (args: Array[String]) {
    
    /** ActorSystem takes a string identifier **/
    val system = ActorSystem("ActorSystemRoot")
    
    /** 
     *  The most simplist way to create a simple actor is
     *  with the actorOf method of ActorSystem; passing
     *  a Props object defining the actor. This method will
     *  instantiate the Actor and return an actorRef object
     *  pointing to the instantiated Actor
     */
    val lifeCycleActor = system.actorOf(Props[LifeCycleHooks],"LifeCycleHooks")
    
    /** sleep a little while just to see the various messages 
     *  generated by the lifecycle methods of the Actor 
     */
    /**
     * The lifeCycleActor will throw an exception when it
     * recieves a restart String message.
     * The default action of the guardian, toplevel supervisor
     * is to restart an actor that throws an exception.
     */
    lifeCycleActor ! "restart" 
    Thread.sleep(1000)
    
    /**
     * Terminating the system, will cause the Actor
     * to be brought down.
     */
    system.terminate()
    Thread.sleep(1000)
  }
}