package org.geo.bdd.exampletests

import akka.actor.{ActorSystem,UnhandledMessage}
import org.scalatest.{WordSpecLike}
import com.goticks.StopSystemAfterAll
//import akka.testkit.{CallingThreadDispatcher, EventFilter, TestKit }
import akka.testkit.{TestKit }
import org.geo.bdd.src.exampletests.Greeter02


class Greeter02Test extends TestKit (ActorSystem("testsystem"))
with WordSpecLike
with StopSystemAfterAll
{
  "The Greeter" must {
    import org.geo.bdd.src.exampletests.Greeting

    "say Hello World! when a Greeting(\"World\") is sent to it" in {
      val props = Greeter02.props(Some(testActor))
      val greeter = system.actorOf(props,"greeter02-1")
      greeter ! Greeting("World")
      expectMsg("Hello World!")
    }
    
    "say something else and see what happens" in {
      val props = Greeter02.props(Some(testActor))
      val greeter = system.actorOf(props, "gteeter02-2")
      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])
      greeter ! "World"
      expectMsg(UnhandledMessage("Worlx", system.deadLetters,greeter))
    }
  }
}