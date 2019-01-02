package org.geo.bdd.exampletests


import com.goticks.StopSystemAfterAll

import org.scalatest.{WordSpecLike, MustMatchers}
import akka.testkit.TestKit
import akka.actor._
import org.geo.bdd.src.exampletests.SilentActor
import akka.testkit.TestActorRef

/**
 * @author george
 * Note that the self type of StopSystemAfterAll requires 
 * that this unit test extend TestKit. 
 *
 */
class SilentActor01Test extends TestKit (ActorSystem("testsystem"))
    with WordSpecLike 
    with MustMatchers 
    /** makes sure the system is stopped after all tests **/
    with StopSystemAfterAll {
  
  "A Silent Actor" must {
    import org.geo.bdd.src.exampletests.SilentActor._

    /** each indivicual test begins with the "in" **/
    "change state when it receives a message, single threaded" in {
      
    val silentActor = TestActorRef[SilentActor]
    silentActor ! SilentMessage("whisper")
      silentActor.underlyingActor.state must (contain("whisper"))
    }
    "change internal state when it receives a message, multi" in {
      
      val silentActor = system.actorOf(Props[SilentActor], "s3")
      silentActor ! SilentMessage("whisper1")
      silentActor ! SilentMessage("whisper2")
      silentActor ! GetState(testActor)
      expectMsg(Vector("whisper1", "whisper2"))
    }
  }
  
}