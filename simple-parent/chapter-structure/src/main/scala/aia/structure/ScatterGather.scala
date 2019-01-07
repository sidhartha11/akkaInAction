package aia.structure

import java.util.Date
import java.text.SimpleDateFormat

import akka.actor.{ActorRef,Actor,Props}

/** for ListBuffer , a mutable class **/
import scala.collection.mutable.ListBuffer

/** for Duration **/
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

//import scala.concurrent.duration._

/**
 * EIP -- Enterprise Integration Pattern Example
 * Scatter/Gather
 * All actors in the example will process the following message type:
 * PhotoMessage.
 * This example will use task parallelization.
 * The original message is basically scattered to all participating Actors that
 * will perform a substask. In this case, GetTime and GetSpeed
 * Scala case classes all implement the Serializable interface so such messages
 * can be sent across the network
 */
case class PhotoMessage ( id: String ,
    photo: String , 
    creationTime: Option[Date] = None ,
    speed: Option[Int] = None )
    
object ImageProcessing {
  val dateFormat = new SimpleDateFormat("ddMMyyy HH:mm:ss:SSS")
  
  def getSpeed ( image: String): Option[Int] = {
    /** Array[String] returned from split **/
    val attributes = image.split('|')
    /** second element is the speed **/
    if ( attributes.size == 3 )
      Some(attributes(1).toInt)
    else 
      None
  }
  
  def getTime(image: String): Option[Date] = {
    val attributes = image.split('|')
    if (attributes.size == 3 ) 
      Some(dateFormat.parse(attributes(0)))
    else
      None
  }
  def getLicense ( image: String ): Option[String] = {
    val attributes = image.split('|')
    if (attributes.size == 3)
      Some(attributes(2))
    else 
      None
  }
  
  def createPhotoString(date: Date, speed: Int): String = {
      createPhotoString(date,speed,"")
  }
  
  def createPhotoString(date: Date , 
      speed: Int,
      license: String): String = {
    "%s|%s|%s".format(dateFormat.format(date),speed,license)
  }
}
/**
 * GetSpeed is a parallel task to determine the the speed of the underlying
 * photo. This is part of the Scatter portion of the pattern.
 * GetSpeed actor will only recevive messages of type PhotoMessage. It will determine
 * the internal speed of the PhotoMessage and add that to the Message. Then 
 * it will send the message down the pipe to the input ActorRef of GetSpeed.
 * Note that the PhotoMessage itself is not changed. A new copy of that message is 
 * actually sent to the pipe, only changing the value of the speed component.
 */
class GetSpeed(pipe: ActorRef) extends Actor {
  def receive = {
    case msg: PhotoMessage => {
      pipe ! msg.copy(
          speed = ImageProcessing.getSpeed(msg.photo))
    }
  }
}

class GetTime(pipe: ActorRef) extends Actor {
  def receive = {
    case msg:PhotoMessage => {
      pipe ! msg.copy(creationTime = 
        ImageProcessing.getTime(msg.photo))
    }
  }
}
/**
 * The recipientlist is a list of actors that will perform some action on the
 * input message and subsequently send the acted upon message down the pipe.
 * The recipientlist represents the "Scatter" part of the Scatter/Gather 
 * pattern.  In this case the recipientList is a hard-coded list of actors;
 * the GetTime and GetSpeed actors that will add to the message before sending it 
 * down the pipe line. Note that here , the identical message, not a copy is 
 * forwared to multiple actors. 
 * 
 */
class RecipientList(recipientList: Seq[ActorRef]) extends Actor {
  def dump(str: String) :Unit = {
    println(Thread.currentThread().getName + ":" + str)
  }
  def receive = {
//    case msg: AnyRef => recipientList.foreach( _ ! msg)
    case msg: AnyRef => recipientList.foreach {
      actor => 
        dump("sending " + msg +  " to " + actor )
        actor ! msg
    }

  }
}
/** special case class for timeout messages **/
case class TimeoutMessage(msg: PhotoMessage)

class Aggregator ( timeout: FiniteDuration, pipe: ActorRef) extends Actor {
  
  val messages = new ListBuffer[PhotoMessage]
  /** for ExecutionContext , use context from Actor Trait **/
  implicit val ec = context.system.dispatcher
  
  override def preRestart(reason: Throwable , message: Option[Any]): Unit = {
    /** invoke parent restart mechanism **/
    super.preRestart(reason,message)
    /** send any queued messages to the restarting actor **/
    messages.foreach(self ! _)
    /** initialize the message ListBuffer **/
    messages.clear()
  }
  
  def receive = {
    /** only accept messages of type PhotoMessage **/
    case rcvMsg: PhotoMessage => {
      val m = messages.find{
        _.id == rcvMsg.id
      }
      /**
       * The way this works is based on 2 messages. Both have the same id.
       * If we find a message with that id, that means we are finished. So we
       * combine the missing attributes in the newest message with the old one.
       * And then send it to the pipe, we cleanup by removing the older message.
       * If the message is not found, that means it is , in this case, the very
       * first message with tha unique id. Note that in this contrived example,
       * only 2 messages are considered proper candidates. 
       */
      m match {
        case Some(alreadyRcvMsg) => {
          val newCombinedMsg = new PhotoMessage(
              rcvMsg.id,
              rcvMsg.photo,
              rcvMsg.creationTime.orElse(alreadyRcvMsg.creationTime),
              rcvMsg.speed.orElse(alreadyRcvMsg.speed))
          pipe ! newCombinedMsg
          // cleanup message
          messages -= alreadyRcvMsg
        }
        case None => {
          /** just add th message to the ListBuffer **/
          messages += rcvMsg 
          context.system.scheduler.scheduleOnce(
              timeout,
              self,
              new TimeoutMessage(rcvMsg))
        }
      }
    }
    
    case TimeoutMessage(rcvMsg) =>  {
      messages.find(_.id == rcvMsg.id) match {
        case Some(alreadyRcvMsg) => {
          pipe ! alreadyRcvMsg 
          messages -= alreadyRcvMsg
          }
        case None => // message is already processed
        }
      }
    case ex: Exception => throw ex
    }
  }