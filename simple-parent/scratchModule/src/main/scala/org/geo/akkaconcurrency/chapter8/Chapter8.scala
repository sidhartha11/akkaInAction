package org.geo.akkaconcurrency.chapter8

import akka.actor.{Actor,ActorSystem,Props,Terminated}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._

object Messages {
  case object Initialize
}
/**
 * Chapter 8 examples and illustrations
 */

class MyActor extends Actor {
  import Messages._
  
  /** make MyActor Sleep long enough to monitor the death of Actor2 **/
  
  /**
   * Lifecycle function, preStart.
   * Perform any initialization setup here
   * Often this is a good spot to send yourself a message
   * such as self ! Initialize
   */
  override def preStart(): Unit = {
    println("in preStart()")
    /** watch for the death of MyActor2 **/
    val myActor2 = context.actorOf(Props[MyActor2], "MyActor2")
    context.watch(context.actorOf(Props[MyActor2]))
    self ! Initialize
  }
  
  /**
   * Lifecycle function, postStop.
   * The message pump is shut down
   * so any message you send to yourself will only go to the
   * dead letter office, but if you'd like to clean up any 
   * resources,such as Database sessions, now's the time to
   * do it.
   */
  
  override def postStop(): Unit = {
    println("in postStop")
  }
  
  /**
   * Partial function to receive case class/object messages 
   */
  def receive = {
    // do your usual processing here. For example:
    case Initialize => 
      println("initialization message received")
      Thread.sleep(10000)
      // call your own post start initialization function here
      case Terminated(deadActor) =>
        println("MyActor --> detected dead " + deadActor.path.name + " has died")
  }
}

///**
// * MyActor2 will be death-watched by MyActor
// */
//object MyActor2 {
//  case object Initialize
// }
/**
 * Chapter 8 examples and illustrations
 */

class MyActor2 extends Actor {
  import Messages._
   override val supervisorStrategy =
      OneForOneStrategy(1, 1 minute) {
        case _ => Restart
      }
  /**
   * Lifecycle function, preStart.
   * Perform any initialization setup here
   * Often this is a good spot to send yourself a message
   * such as self ! Initialize
   */
  override def preStart(): Unit = {
    println("MyActor2 in preStart()")
    self ! Initialize
  }
  
  /**
   * Lifecycle function, postStop.
   * The message pump is shut down
   * so any message you send to yourself will only go to the
   * dead letter office, but if you'd like to clean up any 
   * resources,such as Database sessions, now's the time to
   * do it.
   */
  
  override def postStop(): Unit = {
    println("MyActor2 in postStop")
  }
  
  /**
   * Partial function to receive case class/object messages 
   */
  def receive = {
    // do your usual processing here. For example:
    case Initialize => 
      println("MyActor2 initialization message received")
      Thread.sleep(3000)
      val x = 3 / 0 
      // call your own post start initialization function here
  }
  
  /** control restarting **/
   override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor2 in preRestart: (%s,%s)".format(reason,message))
    context.children foreach context.stop
    // postStop()
  }
  
  override def postRestart(reason: Throwable) {
    println("actor2 in postRestart, suppresing preStart")
    context.system.stop(self)
    // preStart()
  }
}
object Chapter8 extends App {
  
  /** start up MyActor for a test run **/
   implicit val timeout = Timeout(5 seconds)
  /**
   * Creates a system of actors used to create child actors
   * Note that this is where the user guardian lives
   */
  val system = ActorSystem("MyActorTesting")
  /** create a actorRef to the Plane object **/
//  val myActor2 = system.actorOf(Props[MyActor2], "MyActor2")
//  Thread.sleep(2000)
  val myActor = system.actorOf(Props[MyActor], "MyActor")
  Thread.sleep(12000)
  system.terminate()
  
  
}