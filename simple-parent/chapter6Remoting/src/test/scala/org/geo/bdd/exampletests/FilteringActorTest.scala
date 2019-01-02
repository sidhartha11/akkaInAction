package org.geo.bdd.exampletests
/**
 * trait StopSystemAfterAll extends BeforeAndAfterAll
 * This trait will make sure that the actorsystem is shut down
 * after the test completes
 */
import com.goticks.StopSystemAfterAll
import akka.testkit.TestKit
import akka.actor.{Props,ActorRef,Actor,ActorSystem}
import org.scalatest.{WordSpecLike,MustMatchers}
import scala.util.Random
import org.geo.bdd.src.exampletests.FilteringActor
import org.geo.bdd.src.exampletests.FilteringActor._


class FilteringActorTest extends TestKit (ActorSystem("testsystem"))
with WordSpecLike
with MustMatchers
with StopSystemAfterAll 
{
  "A Filtering Actor" must {
    "filter out particular messages" in {
      // import org.geo.bdd.src.exampletests.FilteringActor._
      
      /** create a Props object for the testActor **/
      val props = FilteringActor.props(testActor, 5)
      /** create a test FilteringActor using implicit system val **/
      val filter = system.actorOf(props,"filter-1")
      /** send several messages to see if dups are ignored **/
      filter ! Event(1)
      filter ! Event(2)
      filter ! Event(1)
      filter ! Event(3)
      filter ! Event(1)
      filter ! Event(4)
      filter ! Event(5)
      filter ! Event(5)
      filter ! Event(6)
      
      /** using receiveWhile, get a list of ids processed **/
      /** this partial function will receive the events sent to the testActor **/
      val eventIds = receiveWhile() {
        case Event(id) if id <= 5 => id 
      }
      
      eventIds must be(List(1,2,3,4,5))
      expectMsg(Event(6))
      }
    "filter out particular messages using expectNoMsg" in {
      val props = FilteringActor.props(testActor,5)
      val filter = system.actorOf(props,"filter-2")
      
      filter ! Event(1)
      filter ! Event(2)
      expectMsg(Event(1))
      expectMsg(Event(2))
      filter ! Event(1)
      expectNoMessage
      filter ! Event(3)
      expectMsg(Event(3))
      filter ! Event(1)
      expectNoMessage
      filter ! Event(4)
      filter ! Event(5) 
      filter ! Event(5)
      expectMsg(Event(4))
      expectMsg(Event(5))
      expectNoMessage
      
    }
    }
  }