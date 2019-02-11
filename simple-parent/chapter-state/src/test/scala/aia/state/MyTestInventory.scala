package aia.state

import akka.testkit.{TestProbe, TestKit }
import akka.actor.{ActorSystem, ActorLogging,Props}
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import akka.actor.FSM.SubscribeTransitionCallBack
import akka.actor.FSM.CurrentState
import akka.actor.FSM.Transition

class MyTestInventory  extends 
TestKit(ActorSystem("MyTestInventory"))
with WordSpecLike with BeforeAndAfterAll with MustMatchers {
  
  "Subscribing to get FSM Events" must {
    "probe will subscribe to Transitions" in {
      
      
      /** create a publisher actor first **/
      val publisher = system.actorOf(Props(new Publisher(2,2)))
      /**
       * Create an inventory actor passing the publisher as 
       * constructor argument
       */
      val inventory = system.actorOf(Props( new Inventory(publisher)))
      /** create a test probe next **/
      val stateProbe = TestProbe()
      val replyProbe = TestProbe()
      /** subscribe for FSM transition events **/
      inventory ! new SubscribeTransitionCallBack(stateProbe.ref)
      stateProbe.expectMsg(new CurrentState(inventory,WaitForRequests))
      
      /** send a book request **/
      /** this should trigger state changes **/
      inventory ! new BookRequest("context1", replyProbe.ref)
      stateProbe.expectMsg(
          new Transition(inventory, WaitForRequests, WaitForPublisher))
      stateProbe.expectMsg(
          new Transition(inventory, WaitForPublisher, ProcessRequest))
      stateProbe.expectMsg(
          new Transition(inventory, ProcessRequest, WaitForRequests))
      replyProbe.expectMsg(new BookReply("context1" , Right(1)))
      
    }
  }
  
}