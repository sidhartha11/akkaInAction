package zzz.akka.avionics.parallel

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
import org.scalatest.ParallelTestExecution
import akka.actor.ActorRef
import zzz.akka.investigation.MyActor


// It is helpful to create a base class for your tests
class TestKitSpec(actorSystem: ActorSystem) extends 
TestKit(actorSystem) 
with WordSpecLike
with Matchers
with BeforeAndAfterAll

class MyActorSpec extends 
TestKitSpec(ActorSystem("MyActorSpec")) {
//    with ParallelTestExecution {
  println("in MyActorSpec Constructor")
  override def afterAll() {
    system.terminate()
  }
  
  def makeActor(): ActorRef = {
    println("making actor")
    system.actorOf(Props[MyActor], "MyActor")
  }
   
  /** 
   *  need to import the singleton MyActor object to 
   *  have access to the Ping and Pong case objects
   */
    
  import MyActor._
  "My Actor" should {
    "throw if constructed with the wrong name" in {
   //   an [Exception] should be thrownBy  {
        println("in first test")
        val a = system.actorOf(Props[MyActor])
   //   }
    }
    "construct without exception" in {
      println("in second test")
      val a = makeActor()
      // The throw will cause the test to fail 
    }
    "respond with a Pong to a Ping" in {
      println("in third test")
      val a = makeActor()
      a ! Ping
      expectMsg(Pong)
    }
  }
  
}