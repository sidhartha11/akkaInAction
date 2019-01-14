package org.geo.lightbend.simple

import akka.actor.{ Actor, ActorSystem, Props, ActorRef, Terminated }
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.pattern.ask
import akka.routing.RoundRobinRoutingLogic
import java.io.File
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.util.Timeout

import aia.routing.DirectoryTraversal._
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import org.geo.image.processor._ 
import akka.routing.FromConfig
import akka.actor.ActorLogging

case class Work(msg: String)
case class WorkFile(msg: File)
//case class FileLocation(fileno: String, x: Int, y: Int, bi: BufferedImage)

class Worker() extends Actor {
  var id = self.path.name

  println("Worker initialized: id= %s".format(id))

  def receive = {

    case msg =>
      println("processing message: %s".format(msg))
      sender() ! msg
  }
}

class IWorker(pipe: ActorRef) extends Actor with ActorLogging {
  var id = self.path.name

  log.info("IWorker initialized: id= %s".format(id))

  def receive = {

    case msg: FileLocation =>
      log.info("IWorker,FileLocation : Received message {} in Actor {}" , msg , self.path.name)
      pipe forward  msg.copy(bi = Some(getImage(msg)))
      
    case msg: Finished =>
      log.info("IWorker,Finished : Received message {} in Actor {}" , msg , self.path.name)
      pipe forward msg
  }
  
  def getImage(msg: FileLocation): BufferedImage = {
    ImageIO.read(new File(msg.fileno))
  }
}

class Master extends Actor {

  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[Worker])
      context watch r
      ActorRefRoutee(r)
    }

    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case w: Work =>
      println("router will route message")
      router.route(w, sender())
    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[Worker])
      context watch r
      router = router.addRoutee(r)
  }
}

object TestMaster extends App {
  def dir = "C:\\workarea\\fixed2\\"
  def doSimple = {
     
  /** create an ActorSystem to send a message **/

  val system = ActorSystem("TestMaster")
  
  val router = system.actorOf(Props[Worker], "master")
  implicit val timeout = Timeout(5 seconds)

  val s = scan3(new File("C:/bin")) {
    t =>
      val future = router ? WorkFile(t)
      val result = Await.result(future, timeout.duration).asInstanceOf[WorkFile]
      println("got back message:" + result)
  }
  system.terminate()
  }
  
  def doSimple2 = {
  /** create an ActorSystem From which to create Actors **/
  val system = ActorSystem("TestMaster")
  
  /**
   * create an actor that will generate the new image.
   */
  //  val numberCols = 14 
//  val numberRows = 14 
//  val chunkWidth = 581
//  val chunkHeight= 581
  val numberImages = 196
  val numberCols = 14 
  val numberRows = 14 
  val chunkWidth = 560
  val chunkHeight= 560
  val typ = 5 
  val generator = system.actorOf(Props( new IWriter(
    numberImages, /** number of images **/
    chunkWidth, /** width **/
    chunkHeight, /** height **/
    numberCols,  /** numbercols **/
    numberRows,  /** numberrows **/
    typ  /** type **/
      )
      )
      ,"iwriter")
  
  /**
   * Create 
   */
  val router = system.actorOf(Props(new IWorker(generator)), "master")
//  implicit val timeout = Timeout(5 seconds)
  implicit val timeout2 = Timeout(30 seconds)


  (0 until numberRows) foreach {
    r => 
      (0 until numberCols) foreach {
        c => 
          println("filenumber = " + ( numberCols * r + c ))
          println("[" + chunkWidth * c + "," + chunkHeight * r + "]")
          val fileno = "%s%04d.jpg".format(dir, numberCols * r + c)
          val fileObj = FileLocation(fileno, chunkWidth * c, chunkHeight * r, None)
          router ! fileObj
          
//          val future = router ? fileObj
//          val result = Await.result(future, timeout2.duration)
          
//          println("got back message:" + result)
//          if ( ( numberCols * r + c ) == 195 ) {
//          val fileObj = FileLocation("endmessage", -1, -1, None)
//          val future = router ? fileObj
//          val result = Await.result(future, timeout2.duration)
//          println("got back message:" + result)
//          }

      }
  }
  println("sending Finished message")
  router ! Finished("FileFinished.jpg")

//  val future = router ? Finished("FileFinished.jpg")
//  val result = Await.result(future, timeout2.duration)
//  val fileObj = FileLocation("endmessage", -1, -1, None)
//  val future = router ? fileObj
//  val result = Await.result(future, timeout2.duration)
//  println("got back message:" + result)
  
//  		/** scan thru all files **/
//		IntStream.range(0, nmbrRows).forEach(r -> {
//			IntStream.range(0, nmbrCols).forEach(c -> {
//				System.out.println((nmbrCols * r + c) + "= filenumber");
//				System.out.println("[" + chunkWidth * c + "," + chunkHeight * r + "]");
//
//				/** add a rect to the underlying image **/
//				String fileno = String.format("%s%04d.jpg", dir, nmbrCols * r + c);
////				String fileno = String.format("%senhancedA%d.jpg", dir, nmbrCols * r + c);
//				iWriter.addRect(fileno, chunkWidth * c, chunkHeight * r);
//			});
//		});

//  val s = scan3(new File("C:/bin")) {
//    t =>
//      val future = router ? WorkFile(t)
//      val result = Await.result(future, timeout.duration).asInstanceOf[WorkFile]
//      println("got back message:" + result)
//  }
  system.terminate()
  }
  
  /** create an ActorSystem From which to create Actors **/
  val system = ActorSystem("TestMaster")
  
  /**
   * IWriter actor
   * This actor acts as a recipient of all the messages generated by 
   * the Router's set of routees 
   * It basically writes the small image rect to the internal image
   * buffer until all the rects are consummed. And once all are consumed,
   * the complete image is written to disk.
   * 
   */
  val numberImages = 196
  val numberCols = 14 
  val numberRows = 14 
  val chunkWidth = 560
  val chunkHeight= 560
  val typ = 5 
  val generator = system.actorOf(Props( new IWriter(
    numberImages, /** number of images **/
    chunkWidth, /** width **/
    chunkHeight, /** height **/
    numberCols,  /** numbercols **/
    numberRows,  /** numberrows **/
    typ  /** type **/
      )
      )
      ,"iwriter")
  
  /**
   * This router is defined in the configuration file. It is defined as a 
   * balancing pool Router:
   *    actor {
   *    deployment {
   *            /poolRouter {
   *            router = balancing-pool
   *            nr-of-instances = 5 
   *        }
   *    }
   *    
   *    The balancing-pool will write to at most 5 instances of 
   *    the IWorker actor.
   */
  val router = system.actorOf(
      FromConfig.props(Props(new IWorker(generator))),"roundRobinRouter")
      
  implicit val timeout2 = Timeout(30 seconds)


  (0 until numberRows) foreach {
    r => 
      (0 until numberCols) foreach {
        c => 
          println("filenumber = " + ( numberCols * r + c ))
          println("[" + chunkWidth * c + "," + chunkHeight * r + "]")
          val fileno = "%s%04d.jpg".format(dir, numberCols * r + c)
          val fileObj = FileLocation(fileno, chunkWidth * c, chunkHeight * r, None)
          router ! fileObj

      }
  }
  println("sending Finished message")
//  val future = router ? Finished("FileFinished.jpg")
//  val result = Await.result(future, timeout2.duration)
  router ! Finished("FileFinished234.jpg")
  
}