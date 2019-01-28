package org.geo.router.statebased

import scala.concurrent.duration._
import akka.testkit.{TestProbe, TestKit}
import akka.actor.{ActorSystem,Props}

import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SwitchRouterTest 
extends TestKit(ActorSystem("SwitchRouterTest"))
with WordSpecLike with BeforeAndAfterAll 
{
  
  override def afterAll() = {
    system.terminate()
  }
  
  "State Based Actor" must {
    "implement and on and off transition" in {
      val normalFlowProbe = TestProbe()
      val cleanupProbe = TestProbe()
      
      val router = system.actorOf(
          Props( new SwitchRouter(
              normalFlow = normalFlowProbe.ref,
              cleanUp = cleanupProbe.ref)))
              
      val msg = "message" 
      router ! msg
      
      cleanupProbe.expectMsg(msg)
      
      normalFlowProbe.expectNoMessage(1 second)
      
      router ! RouteStateOn
      
      router ! msg 
      
      cleanupProbe.expectNoMessage(1 second )
      normalFlowProbe.expectMsg(msg)
      
      router ! RouteStateOff 
      router ! msg 
      cleanupProbe.expectMsg(msg)
      normalFlowProbe.expectNoMessage(1 second)
      
    }
  }
}