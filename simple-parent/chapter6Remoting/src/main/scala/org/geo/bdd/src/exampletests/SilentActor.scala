package org.geo.bdd.src.exampletests
import akka.actor.{Actor,ActorRef}
object SilentActor {
  case class SilentMessage(data: String)
  case class GetState(receiver: ActorRef)
}
class SilentActor extends Actor {
  import SilentActor._
  var internalState = Vector[String]()
  
  def receive = {
    case SilentMessage(data) => 
      {
      internalState = internalState :+ data
      println("state = ${data}")
      }
      
    case GetState(receiver) => receiver ! internalState 
  }
  
  def state = internalState 
}