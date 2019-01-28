package org.geo.router.hashing

import scala.concurrent.duration._
import akka.routing.ConsistentHashingRouter._
import akka.actor.{ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import akka.actor.Props
import akka.routing.{ConsistentHashingPool}

class HashRoutingTest extends TestKit(ActorSystem("PerfRoutingTest"))
with WordSpecLike with BeforeAndAfterAll {
  
  override def afterAll() = {
    system.terminate()
  }
  
  "The HashRouting" must {
    "work using mapping" in {
      
      /** create a test probe as recient **/
      val endProbe = TestProbe()
      
      /*
       * User ConsistentHashMapping to direct
       * router to correct routee 
       * Message type if a super type of multiple
       * messagei implementations
       */
      def hashMapping: ConsistentHashMapping = {
        case msg: GatherMessage => msg.id 
      }
      
      /**
       * create a router using the ConsistentHashingPool 
       * Note the function, hashMapping , is the device that implements
       * routing control
       */
      val router = system
      .actorOf(
          ConsistentHashingPool(10, virtualNodesFactor = 10 , hashMapping = hashMapping)
          .props(Props(new SimpleGather(endProbe.ref))), name ="routerMapping")
      /** send only one message with id 1 **/   
      router ! GatherMessageNormalImpl("1" , Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      
      router ! GatherMessageNormalImpl("1", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("1" , Seq("msg1", "msg2")))
      
      router ! GatherMessageNormalImpl("10" , Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageNormalImpl("10", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("10" , Seq("msg1","msg2")))
      system.stop(router)
    }
    "work using messages" in {
      val endProbe = TestProbe()
      
      val router = system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10)
          .props(Props(new SimpleGather(endProbe.ref))), name = "routerMessage")
          
          router ! GatherMessageWithHash("1" , Seq("msg1"))
          endProbe.expectNoMessage(100.millis)
          
          router ! GatherMessageWithHash("1", Seq("msg2"))
          endProbe.expectMsg(GatherMessageNormalImpl("1", Seq("msg1","msg2")))
          router ! GatherMessageWithHash("10" , Seq("msg1"))
          endProbe.expectNoMessage(100.millis)
          router ! GatherMessageWithHash("10", Seq("msg2"))
          endProbe.expectMsg(GatherMessageNormalImpl("10", Seq("msg1", "msg2")))
          system.stop(router)
    }
    "work using Envelope" in {
      val endProbe = TestProbe()
      val router = 
        system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10)
            .props(Props(new SimpleGather(endProbe.ref))), name = "routerEnvelope")
      
      router ! ConsistentHashableEnvelope(
          message = GatherMessageNormalImpl("1", Seq("msg1")),
          hashKey = "someHash")
      
          endProbe.expectNoMessage(100.millis)
       router ! ConsistentHashableEnvelope(
           message = GatherMessageNormalImpl("1", Seq("msg2")),
           hashKey = "someHash")
           endProbe.expectMsg(GatherMessageNormalImpl("1" , Seq("msg1","msg2")))
           
    }
  }
}