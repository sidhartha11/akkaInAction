package com.goticks.book.examples

import scala.util.{ Failure, Success }
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._

import com.goticks.utilities.Utils._
import akka.actor.Props
import akka.actor.ActorSystem
/**
 * Simple Illustration of Actor tell and ask message sending
 * which is respectively ! and ? symbols.
 * This example was created in Eclipse and you need to kill the application
 * in order to force it to exit. This is because the ActorSystem remains active
 * unless you kill this application or programmatically terminate the application.
 * There are many ways to do that: No need for this simple example.
 *
 */
class SimpleActor extends Actor {
  emitt("SimpleActor CTR")
  def receive = {
    case "Hello" => emitt("got default:" + "Hello")
    case 290     => emitt("message received 290")
    case 330 =>
      emitt("got 330, will send response back")
      sender() ! 660
    case e => emitt("unknown message received:" + e)
  }
}
object RemoteDeploymentExercises extends App {

  /**
   * First create the standard actorsystem
   */
  implicit val system = ActorSystem("simpleactorsystem")
  /**
   * Second create an actor based on the SimpleActor class
   * The local path to this actor would be /simpleActor, 
   * omitting the user guardian actor. 
   */
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  /**
   * For processing Futures below, we need an execution context.
   * The dispatcher of ActorSystem can be used as a default execution
   * context by declaring it implicit below.
   */
  implicit val ec = system.dispatcher // bindingFuture.map requires an implicit ExecutionContext
  /**
   * Now send a simple message to the actor
   * Need to import a few things for this to work:
   * akka.pattern.ask pattern
   * import scala.concurrent.duration._
   * import akka.util.Timeout
   *
   */

  implicit val timeout = Timeout(5 seconds)

  /**
   * Send a simple string to the actor using a tell message
   */
  simpleActor ! "Hello"
  /**
   * send a simple numbe to the actor using a tell message
   */
  simpleActor ! 290
  /**
   * send a number to the actor using an ask message and process
   * the result that comes back in the form of a Future
   */
  val r = simpleActor ? 330
  emitt("got a future back:" + r)
  /**
   * Since a future came back we can process it when it completes
   */
  r.map(v => println("got a %s".format(v)))
  /**
   * just display a bye message
   */
  emitt("bye")
}