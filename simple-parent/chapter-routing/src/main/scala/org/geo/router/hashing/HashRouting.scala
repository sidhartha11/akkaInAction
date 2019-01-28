package org.geo.router.hashing

import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.routing.ConsistentHashingRouter.ConsistentHashable


trait GatherMessage {
  val id: String
  val values: Seq[String]
}

case class GatherMessageNormalImpl(id: String, values: Seq[String]) extends GatherMessage

/**
 * This message contains the definition of the key used by the rounting to 
 * direct the message to the proper routee.
 */
case class GatherMessageWithHash(id: String, values: Seq[String]) extends GatherMessage with ConsistentHashable {
  override def consistentHashKey: Any = id
}

class SimpleGather(nextStep: ActorRef) extends Actor with ActorLogging  {
  var messages = Map[String, GatherMessage]()
  def receive = {
    case msg: GatherMessage => {
      messages.get(msg.id) match {
        case Some(previous) => {
          //join
          nextStep ! GatherMessageNormalImpl(msg.id, previous.values ++ msg.values)
          log.info("Joined: "+ msg.id + " by "+ self.path.name)
          messages -= msg.id
        }
        case None => messages += msg.id -> msg
      }
    }
  }
}