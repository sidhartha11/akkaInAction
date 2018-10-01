package zzz.akka.investigation

import akka.actor.{ Actor, Props, ActorSystem }
import akka.actor.actorRef2Scala

class PrintingActor  extends Actor {
  
  def receive = {
    case msg => println(msg)
  }
  
}

object PrintingActorMain {
  /** create an actor system **/
  val system = ActorSystem("PrintingActor")
  val actor = system.actorOf(Props[PrintingActor])
  
  def send(msg: String) {
    println("sending message to actor:" + msg)
    /** send a message to the actor **/
    actor ! msg 
  }
  def main(args: Array[String]) {
    send("printing this message")
    system.terminate
  }
}