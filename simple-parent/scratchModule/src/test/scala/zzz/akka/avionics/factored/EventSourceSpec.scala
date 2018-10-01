package zzz.akka.avionics.factored

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
// We can't test a "trait" very easily, so we're going to 
// create a specific EventSource derivation that conforms to
// the requirements of the trait so that we can test the 
// prodction code.
// here we create an actor for testing by extending
// Actor and the ProductionEventSource that we refactored 
class TestEventSource extends Actor 
    with ProductionEventSource {
  /** override the Actor recieve method **/
  def receive = eventSourceReceive
}

// "class"Spec is a decent convention we'll be following

class EventSourceSpec extends
    TestKit(ActorSystem("EventSourceSpec")) 
        with WordSpecLike 
        with Matchers 
        with BeforeAndAfterAll {
        
  import EventSource._
  override def afterAll() {
     system.terminate()
  }
  
  "EventSource" should {
    "allow us to register a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      /** here we can directly invoke the internal functions of the test Actor **/
      /** here we can look directly at the internal components of the test Actor **/
      /** send a RegisterListener(testActor) message to the real actor **/
      real.receive(RegisterListener(testActor))
      /** inspect the listeners of the real actor **/
      real.listeners should contain (testActor)
    }
    
    "allow us to unregister a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      real.receive(RegisterListener(testActor))
      real.receive(UnregisterListener(testActor))
      real.listeners.size should be (0)
    }
    
    "send the event to our test actor" in {
      val testA = TestActorRef[TestEventSource] 
      testA ! RegisterListener(testActor)
      testA.underlyingActor.sendEvent("Fibonacci")
      expectMsg("Fibonacci")
    }
  }
}