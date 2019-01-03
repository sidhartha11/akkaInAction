package aia.structure

import akka.actor.{ActorSystem,Props}
import akka.testkit._

/**
 * get time unit dsl
 */
import scala.concurrent.duration._

/**
 * get WordSpecLike and BeforeAndAfterAll
 */
import org.scalatest._
/**
**
**/
class PipeAndFilterTest 
extends TestKit(ActorSystem("PipeAndFilterTest"))
with WordSpecLike
with BeforeAndAfterAll {
  
  val timeout = 2 seconds
  
  override def afterAll(): Unit = {
    system.terminate()
  }
  
  /**
   * The actual test definitions follow here
   */
  "The pipe and filter" must {
    "filter messages in configuration 1" in {
      val endProbe = TestProbe()
      
      /**
       * SpeedFilter is an actor constructed with a minimum speed and
       * an actorRef to pipe the message to if the speed test passes
       * Here the actorRef is the TestProbe
       */
      val speedFilterRef = system.actorOf(
          Props(new SpeedFilter(50, endProbe.ref)))
      
      /**
       * LicenseFilterRefis an actor constructed with the speedFilterRef.
       * It will check for a non empty license and pass the message 
       * onto the speedFilterRef. 
       */
      val licenseFilterRef = system.actorOf(
          Props(new LicenseFilter(speedFilterRef)))
      /**
       * create a Photo message to pipe around
       */
      val msg = new Photo("123xyz",60)
      
      /*
       * First we pass the message with speedlimit of 60 to the licenseFilterRef
       * And a valid license photo. Since the Photo check succeeds, the message
       * is passed on to the TestProb
       */
      licenseFilterRef ! msg 
      /**
       * endProbe is checked to make sure it got the message. 
       */
      endProbe.expectMsg(msg)
      
      /**
       * Next we pass a bad message with an empty liscense component.
       */
      licenseFilterRef ! new Photo("", 60)
      endProbe.expectNoMessage(timeout)
      
      /**
       * Next we pass a message with a speed limit that does not pass the minimum
       * check
       */
      
      licenseFilterRef ! new Photo("123xyz", 49)
      endProbe.expectNoMessage(timeout)
    }
    
    "filter messages in configuration 2" in {
      val endProbe = TestProbe()
      val licenseFilterRef = system.actorOf(
          Props(new LicenseFilter(endProbe.ref)))
          
      val speedFilterRef = system.actorOf(
          Props(new SpeedFilter(50, licenseFilterRef)))
          
      val msg = new Photo("123xyz", 60)
      speedFilterRef ! msg
      endProbe.expectMsg(msg)
      
      speedFilterRef ! new Photo("", 60)
      endProbe.expectNoMessage(timeout)
      
      speedFilterRef ! new Photo("123xyz", 49)
      endProbe.expectNoMessage(timeout)
      
    }
  }
}
