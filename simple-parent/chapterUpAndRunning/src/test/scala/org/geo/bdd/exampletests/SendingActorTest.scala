package org.geo.bdd.exampletests

import com.goticks.StopSystemAfterAll

import scala.util.Random
import akka.testkit.TestKit
import akka.actor.{ Props, ActorRef, Actor, ActorSystem }
import org.scalatest.{WordSpecLike, MustMatchers}


class SendingActorTest extends TestKit (ActorSystem("testsystem"))
with WordSpecLike
with MustMatchers
/** makes sure the system is stopped after all tests **/
with StopSystemAfterAll {
  
  "A Sending Actor" must {
    "send a message to another actor when it has finished processing" in{
      import org.geo.bdd.src.exampletests.SendingActor._ 
      
      val props = org.geo.bdd.src.exampletests.SendingActor.props(testActor)
      /** create a actor that contains the testActor for testing **/
      val sendingActor = system.actorOf(props, "sendingActor")
      
      val sizex = 1000
      val maxInclusive = 100000
      
      def randomEvents() = ( 0 until sizex).map{ _ => 
        Event(Random.nextInt(maxInclusive))}.toVector
      
      val unsorted = randomEvents()
      val sortEvents = SortEvents(unsorted)
      sendingActor ! sortEvents
      
     expectMsgPF() {
        case SortedEvents(events) =>
          events.size must be(sizex)
          unsorted.sortBy(_.id) must be(events)
      }
    }
  }
  
}