package aia.structure

import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.{ActorSystem,Props}
import akka.testkit.TestProbe


/**
 * Scala Test Code to test the RecipientList processing 
 */
class RecipientListTest 
extends TestKit(ActorSystem("RecipientListTest"))
with WordSpecLike
with BeforeAndAfterAll {
  
  override def afterAll(): Unit = {
    system.terminate
  }
  /**
   * TestProbes are used as recipients of actor messages so that we
   * can assert that the message was actually processed. 
   */
  "The ReciientList" must {
    "scatter the message" in {
      val endProbe1 = TestProbe()
      val endProbe2 = TestProbe()
      val endProbe3 = TestProbe()
      val list = Seq(endProbe1.ref, endProbe2.ref, endProbe3.ref)
      val actorRef = system.actorOf(Props(new RecipientList(list)))
      val msg = "message"
      /**
       * The purpose here is to just check that the recipient list actor is 
       * scattering the messages to multiple actors properly
       */
      actorRef ! msg 
      endProbe1.expectMsg(msg)
      endProbe2.expectMsg(msg)
      endProbe3.expectMsg(msg)
      
    }
  }
  
  
}