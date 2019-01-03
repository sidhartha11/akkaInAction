package aia.structure

import akka.actor.{ActorRef,Actor}

/**
 * create the interface used to pass messages between the
 * pipe to different filters.
 * Here the simple actors simply extend the Actor trait.
 */

case class Photo(license: String, speed: Int)

/**
 * create the process within the pipe, a filter, to 
 * process the Speed of the incoming message
 * If the speed is greater than minSpeed, do not send the message
 * down the pipe
 * 
 */

class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor {
  def receive = {
    case msg: Photo => 
      if ( msg.speed > minSpeed ) 
        pipe ! msg 
  }
}

class LicenseFilter(pipe: ActorRef) extends Actor {
  def receive = {
    case msg: Photo => 
      if (!msg.license.isEmpty )
        pipe ! msg
  }
}
