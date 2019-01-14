package org.geo.image.processor

import akka.actor.{ Actor, PoisonPill, Kill, ActorRef }
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import akka.actor.ActorLogging

/** 
 *  Messages processed by ImageAccumulator actor
 */
object ImageAccumulator {
case class FileLocation(fileno: String, x: Int, y: Int, bi: Option[BufferedImage]=None)
case class Finished(filename: String)
}

/**
 * ImageAccumulator actor
 */
class ImageAccumulator(
  nmbrImages: Int,
  width:      Int,
  height:     Int,
  rows:       Int,
  cols:       Int,
  typ:        Int,
  dir:        String,
  filename:   String
  )
  extends Actor  with ActorLogging {
  
  val finalImg = new BufferedImage(width * cols, height * rows, typ)

  val id = self.path.name
  var imageCount = 0
  var actorRef = self
  log.info("ImageAccumulator initialized: id= {}", id)
  
  def receive = {

    case router: ActorRef =>
      log.info("got router actorRef")
      actorRef = router 
    case msg: FileLocation =>
      
      val x:Int  = msg.x 
      val y:Int  = msg.y
      log.debug("processing message: {}" , msg )
      finalImg.createGraphics().drawImage(msg.bi.get,x,y,null)
      imageCount += 1
      if ( imageCount == nmbrImages ){
        self ! Finished(filename)
      }
  
    case Finished(filename) =>
      log.info("Finished image processing {}", dir + filename)
      ImageIO.write(finalImg, "jpeg", new File(dir + filename));
      log.info("sending PoisonPill to router")
      actorRef ! PoisonPill
      log.info("stopping ImageAccumulator")
      context.stop(self)
      log.info("shutting down system")
      context.system.terminate()
  }

}