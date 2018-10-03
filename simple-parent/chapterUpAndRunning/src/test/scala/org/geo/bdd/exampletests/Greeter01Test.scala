package org.geo.bdd.exampletests

import com.goticks.StopSystemAfterAll
import com.typesafe.config.{ Config, ConfigFactory }
import akka.actor.{Props,ActorSystem}
import akka.testkit.{CallingThreadDispatcher, EventFilter, TestKit }
import org.scalatest.{WordSpecLike}
import Greeter01Test._
import org.geo.bdd.src.exampletests.Greeting
import org.geo.bdd.src.exampletests.Greeter

class Greeter01Test  extends TestKit(testSystem) 
with WordSpecLike
// with MustMatchers
with StopSystemAfterAll
{
  "The Greeter" must {
    "say Hello World! when a Greeting(\"World\") is sent to it" in {
      val dispatcherId = CallingThreadDispatcher.Id
      val props = Props[Greeter].withDispatcher(dispatcherId)
      val greeter = system.actorOf(props)
      EventFilter.info(message = "Hello World!", 
          occurrences = 1).intercept{ 
              greeter ! Greeting("World")
    }
    }
  }
}
object Greeter01Test {
  val testSystem = {
    val config = ConfigFactory.parseString(
        """
          akka.loggers = [akka.testkit.TestEventListener]
        """
        )
    ActorSystem("testsystem",config)
  }
}