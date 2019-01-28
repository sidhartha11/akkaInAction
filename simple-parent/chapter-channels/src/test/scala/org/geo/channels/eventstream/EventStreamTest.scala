package org.geo.channels.eventstream

import scala.concurrent.duration._
import akka.testkit.{TestProbe,TestKit,ImplicitSender}
import akka.actor.{ActorSystem, DeadLetter,Props}
import org.scalatest.{WordSpecLike,BeforeAndAfterAll,MustMatchers}
import akka.actor.PoisonPill

class EventStreamTest extends TestKit(ActorSystem("EventStreamTest")) 
with WordSpecLike with BeforeAndAfterAll with MustMatchers 
with ImplicitSender {
  
  override def afterAll(): Unit = {
    system.terminate() 
  }
  
  "EventStream" must {
    "allow publish,subscribe and unsubscribe" in {
      
      val DeliverOrder = TestProbe()
      val giftModule = TestProbe()
      
      /**
       * Subscribe to receive messages of type Order.
       * giftModule actor will receive messages of type Order.
       */
      system.eventStream.subscribe(
          DeliverOrder.ref,
          classOf[Order])
      system.eventStream.subscribe(
          giftModule.ref , 
          classOf[Order])
      /** create a message to test the EventStream functionality **/
      val msg = Order("me" , "Akka in Action" , 3)
      /** publish the Order message **/
      system.eventStream.publish(msg)
      
      /** now check that all the subscribers got the message **/
      DeliverOrder.expectMsg(msg)
      giftModule.expectMsg(msg)
      
      /** now unsubscribe the giftModule from getting published messages **/
      system.eventStream.unsubscribe(giftModule.ref)
      
      /** publish the message again to see if giftModule gets it **/
      system.eventStream.publish(msg)
      DeliverOrder.expectMsg(msg)
      giftModule.expectNoMessage(3 seconds) 
      
      
    }
    "A custom event bus words as" in {
      val bus = new OrderMessageBus
      val singleBooks = TestProbe() 
      bus.subscribe(singleBooks.ref, false)
      val multiBooks = TestProbe()
      bus.subscribe(multiBooks.ref , true)
      
      val msg = new Order("me" , "Akka in Action", 1)
      bus.publish(msg)
      singleBooks.expectMsg(msg)
      multiBooks.expectNoMessage(3 seconds)
      
      val msg2 = new Order("me" , "Akka in Action", 3)
      bus.publish(msg2)
      singleBooks.expectNoMessage(3 seconds)
      multiBooks.expectMsg(msg2)
    }
    "Subscribing to the Dead Letter Queue" in {
      /**
       * Create a test probe to recieve published dead letters
       */
      val deadLetterMonitor = TestProbe() 
      /**
       * subscribe to the DeadLetter Event Queue
       */
      system.eventStream.subscribe(
          deadLetterMonitor.ref,
          classOf[DeadLetter]
          )
      /**
       * Start up a real echo actor for test purposes. We ill
       * this actor immediately
       */
          
       val actor = system.actorOf(Props[EchoActor],"echo")
          
      /**
       * Now send a poison pill to the actor to kill it 
       */
       
       actor ! PoisonPill 
       
       val msg = new Order("e" , "Akka in Actiion" , 1 ) 
      /**
       * send this Order to the actor: However, not sure how this 
       * can possibly work since actor only processes messages of type
       * ActorRef.
       */
      actor ! msg 
      /**
       * Even if the actor only processes messages of type ActorRef, since
       * it is dead the message should still be routed to the Dead Letter 
       * Queue. 
       */
      val dead = deadLetterMonitor.expectMsgType[DeadLetter] 
      dead.message must be(msg)
      /**
       * testActor comes with the testKit and supposedly allow you to 
       * check if the sending actor was a TestProbe ... aparently 
       */
      dead.sender must be(testActor)
      
      dead.recipient must be(actor)
          
    }
    "send an unmodified message to DeadLetter Object" in {
      val deadLetterMonitor = TestProbe()
      val actor = system.actorOf(Props[EchoActor], "echo")
      
      /**
       * This deadLetterMonitor will be sent deadletters that are sent
       * to the Event Queue.
       */
      system.eventStream.subscribe(
          deadLetterMonitor.ref , 
          classOf[DeadLetter]
          )
          
      val msg = new Order("me" , "Akka in Action", 1)
      val dead = DeadLetter(msg, testActor, actor)
      system.deadLetters ! dead 
      
      deadLetterMonitor.expectMsg(dead)
      system.stop(actor)
    }
  }
  
}