package org.geo.bdd.lifecycleevents

/** import the scalatest hooks **/
import org.scalatest.{WordSpecLike,MustMatchers}

/** import the akka test classes **/
import akka.testkit.{TestKit }

/** import the akka classes **/
import akka.actor.{Props,ActorSystem}
import com.goticks.StopSystemAfterAll
import org.geo.bdd.src.lifecycleevents.LifeCycleHooks
 
class LifeCycleHooksTest extends TestKit (ActorSystem("testsystem"))
with WordSpecLike
with MustMatchers
with StopSystemAfterAll 
{
  "A simple test to illustrate lifecycle hooks" must {
    "display all the life cycle stages" in {
      val testActorRef = system.actorOf(
          Props[LifeCycleHooks], "LifeCycleHooks")
      /** send a restart message to the test actor **/
      /** this will force the actor to throw an exception **/
      testActorRef ! "restart"
      
      /** send a simple message to te actor and let the testActor
       *  get the response
       */
      testActorRef.tell("msg", testActor)
      /** wait the response to come back **/
      expectMsg("msg")
      Thread.sleep(1000)
      
    }
  }
}