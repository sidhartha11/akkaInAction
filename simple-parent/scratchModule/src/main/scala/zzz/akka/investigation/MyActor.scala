package zzz.akka.investigation

import akka.actor.{ Actor, Props, ActorSystem }

/**
 * Author's method of setting up the messages is as follows:
 * put the relevant messages that this Actor will act on in
 * a singleton object
 */
object MyActor {
  case object Ping
  case object Pong
}
/** the messages processed by this actor **/
case class Gamma(g: String)
case class Beta(b: String, g: Gamma)
case class Alpha(b1: Beta, b2: Beta)

class MyActor extends Actor {
  /**
   * In Scala, in order to use the case objects and classes
   * inside a single object requires importing the Singleton
   * object.
   */
  import MyActor._
  println("inside MyActor")
  /** testing context actorFor function **/
  val doesNotExist = context.actorSelection("/user/does/not/exit")
  def sendEvent[T](event: T): Unit = {
    sender() ! event
  }

  def receive = {
    case Ping    => sender() ! Pong
    case Pong    => sender() ! Ping
    case "Hello" => println("Hi")

    case 42 =>
      println("I don't know the question." +
        "Go ask the Earth Mark II.")

    case s: String =>
      println(s"You sent me a string: $s")

    case Alpha(Beta(b1, Gamma(g1)), Beta(b2, Gamma(g2))) =>
      println(s"beta1: $b1, beta2: $b2, gamma1: $g1, gamma2: $g2")

    case _ =>
      println("huh?")

  }
}

object MyActorMain {
  import MyActor._
  /** first create an ActorSystem **/
  val system = ActorSystem("MyActor")
  /** next create a reference to an actor in the system **/
  val actor = system.actorOf(Props[MyActor])

  def send(x: Any) {
    println("sending %s".format(x))
    actor ! x
  }

  def main(args: Array[String]) {
    send(42)
    send("Sammy")
    send(32.4)
    send(Alpha(Beta("older", Gamma("gamma")), Beta("older2", Gamma("gamma"))))
    send(Ping)
  }
}