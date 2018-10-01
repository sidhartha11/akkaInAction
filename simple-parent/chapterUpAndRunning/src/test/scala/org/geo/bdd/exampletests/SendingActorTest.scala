package org.geo.bdd.exampletests

import com.goticks.StopSystemAfterAll

/** needed for the various scala tests **/
import org.scalatest.{WordSpecLike, MustMatchers}
/** needed for the akka test kit, test actors et al **/
import akka.testkit.TestKit
import akka.actor._
import akka.testkit.TestActorRef
import akka.testkit.TestActor
import scala.util.Random

class SendingActorTest extends TestKit (ActorSystem("testsystem"))
    with WordSpecLike 
    with MustMatchers 
    /** makes sure the system is stopped after all tests **/
    with StopSystemAfterAll {
  
  "A Sending Actor" must {
    "send a message to another actor when it ha finished" {
      import org.geo.bdd.src.exampletests.SendingActor._ 
      
      val props = org.geo.bdd.src.exampletests.SendingActor.props(testActor)
      /** create a actor that contains the testActor for testing **/
      val sendingActor = system.actorOf(props, "sendingActor")
      
      val size = 1000
      val maxInclusive = 100000
      
      def randomEvents() = ( 0 until size).map{ _ => 
        Event(Random.nextInt(maxInclusive))}.toVector
      
      val unsorted = randomEvents()
      val sortEvents = SortEvents(unsorted)
      sendingActor ! sortEvents
      
      expectMsgPF() { 
        case SortedEvents(events) => 
          events.size must be(size)
          (unsorted.sortBy(_.id)) must be (events)
      }
      
    }
  }
  
}