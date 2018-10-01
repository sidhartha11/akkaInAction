package org.geo.akkaconcurrency.chapter7

import akka.actor.{Props, ActorSystem, Actor}
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

object SomeActor {
  case object Ping
}

class SomeActor extends Actor {
  import SomeActor._

  def receive = {
    case Ping =>
      println("Pong")
  }
}

object Example extends App {
  val system: ActorSystem = ActorSystem("test-system")

  system.actorOf(Props[SomeActor], "test-actor")

  implicit val resolveTimeout = Timeout(5 seconds)

  val actorRef = Await.result(system.actorSelection("user/test-actorx").resolveOne(), resolveTimeout.duration)
  

  
  actorRef ! SomeActor.Ping
}