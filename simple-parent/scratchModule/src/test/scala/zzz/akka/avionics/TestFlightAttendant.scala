package zzz.akka.avionics

import akka.actor.{Props,ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers 
import com.typesafe.config.ConfigFactory

object TestFlightAttendant {
  /**
   * ok: we extend FlightAttendant and because of the self-typing
   * we are required to also extend the trait, AttendantResponsiveness
   * where we also override the maxResponseTimeMS to 1
   */
  def apply() = 
    new FlightAttendant 
    with AttendantResponsiveness {
    val maxResponseTimeMS = 1 
  }
}
/**
 * Scala Test Section. 
 */
class FlightAttendantSpec extends 
    TestKit(
        ActorSystem("FlightAttendantSpec"
        ,ConfigFactory.parseString("akka.scheduler.tick-duration = 10ms")))
        with ImplicitSender
        with WordSpecLike
        with Matchers {
  
  /** get the messages for FlightAttendant imported here **/
  import FlightAttendant._
  
  "FlightAttendant" should {
    "get a drink when asked" in {
      println("starting test up >>>>>>")
      val a = TestActorRef(Props(TestFlightAttendant()))
      /** send a GetDrink message **/
      a ! GetDrink("soda")
      expectMsg(Drink("soda"))
      println("finished up test >>>>>>")
    }
  }
  
}