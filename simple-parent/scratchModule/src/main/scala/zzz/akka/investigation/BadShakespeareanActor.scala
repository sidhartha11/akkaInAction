package zzz.akka.investigation

import akka.actor.{ Actor, Props, ActorSystem }
import akka.actor.actorRef2Scala

class BadShakespeareanActor extends Actor {
  // The 'Business Logic'

  def receive = {
    case "Good Morning" =>
      println("Him: Forsooth 'tis the 'morn, but mourneth " +
        "for thou doest I do!")
    case "You're terrible" =>
      println("Him: Yup")
  }
}

object BadShakespeareanMain {
  val system = ActorSystem("BadShakesperean")
  val actor = system.actorOf(Props[BadShakespeareanActor], "Shake")
  def send(msg: String) {
    println(s"Me: $msg")
    actor ! msg
    Thread.sleep(100)
  }
  def main(args: Array[String]) {
    send("Good Morning")
    send("You're terrible")
    system.terminate()
  }
}