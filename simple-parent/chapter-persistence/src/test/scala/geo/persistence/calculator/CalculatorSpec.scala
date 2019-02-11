package geo.persistence.calculator

import akka.actor.{ActorSystem}
import akka.testkit.PersistenceSpec
import Calculator._

class CalculatorSpec extends PersistenceSpec(ActorSystem("test")){
  "The Calculator" should {
    "recover last known result after crash" in {
      /**
       * create an instance of the Calculator actor 
       */
      val calc = system.actorOf(Calculator.props,Calculator.name)
      
      calc ! Calculator.Add(1d)
      calc ! Calculator.GetResult
      expectMsg(1d)
      
      calc ! Calculator.Subtract(0.5d)
      calc ! Calculator.GetResult
      expectMsg(0.5d)
      /**
       * interesting way to kill the actors
       */
      killActors(calc)
      
      val calcResurrected = system.actorOf(Calculator.props, Calculator.name)
      calcResurrected ! Calculator.GetResult
      expectMsg(0.5d)
      
      calcResurrected ! Calculator.Add(1d)
      calcResurrected ! Calculator.GetResult
      expectMsg(1.5d)
      
    }
  }
}