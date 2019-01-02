package org.geo.akkainaction.chapterfutures.illustrations

import org.geo.akkainaction.chapterfutures.illustrations.SimpleExampleFunctions._
import org.geo.akkainaction.chapterfutures.UtilityFunctions._
import org.geo.akkainaction.chapterfutures.entities.CaseEntities._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.concurrent.CountDownLatch

/** needed to execute the Future Call and the Await Call **/
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object LeadUpExamples extends App {

  /** synchronous call illustration **/
  def exampleOfSynchronousCall {
    val ticketNr: Long = 10
    val request = EventRequest(ticketNr)
    val response: EventResponse = callEventService(request)
    val event: Event = response.event
    emitt("event = %s".format(event))
  }

  def exampleGetEventAsyn(): Future[Event] = {
    emitt("enter exampleGetEventAsyn()")
    val ticketNr: Long = 10
    val request = EventRequest(ticketNr)
    /** code block argument is passed by name **/
    /** Future.apply { code block } **/
    val futureEvent: Future[Event] = Future {
      val response = callEventService(request)
      val e = response.event
      emitt("e = " + e)
      response.event
    }
    futureEvent
  }

  def exampleOfAsynchronousCall {
    val latch: CountDownLatch = new CountDownLatch(1)
    val ticketNr: Long = 10
    val request = EventRequest(ticketNr)
    /** code block argument is passed by name **/
    /** Future.apply { code block } **/
    val futureEvent: Future[Event] = Future {
      val response = callEventService(request)
      val e = response.event
      emit("e = " + e)
      latch.countDown()
      response.event
    }
    latch.await()
  }

  def exampleOfAsynchronousCallAwait(tm: Long) {
    val ticketNr: Long = 10
    val request = EventRequest(ticketNr)
    /** code block argument is passed by name **/
    /** Future.apply { code block } **/
    val futureEvent: Future[Event] = Future {
      val response = callEventService(request)
      val e = response.event
      emit("e = " + e)
      response.event
    }
    Await.result(futureEvent, tm seconds)
    futureEvent.map(v => emitt("value is " + v))
  }

  def exampleHandlingEventResult(latch: CountDownLatch): Future[Route] = {
    emitt("enter exampleHandlingEventResult")
    val futureEvent: Future[Event] = exampleGetEventAsyn
    /**
     * futureEvent.map  --> Future[T]
     * futureEvent.foreach --> Unit
     */
    futureEvent.map { event =>
      val trafficRequest = TrafficRequest(
        destination = event.location,
        arrivalTime = event.time)
      val trafficResponse = callTrafficService(trafficRequest)
      emitt("trafficResponse.route=%s".format(trafficResponse))
      latch.countDown()
      trafficResponse.route
    }
  }

  def exampleHandlingEventResultForEach(latch: CountDownLatch): Unit = {
    emitt("enter exampleHandlingEventResult")
    val futureEvent: Future[Event] = exampleGetEventAsyn
    /**
     * futureEvent.map  --> Future[T]
     * futureEvent.foreach --> Unit
     */
    futureEvent.foreach { event =>
      val trafficRequest = TrafficRequest(
        destination = event.location,
        arrivalTime = event.time)
      val trafficResponse = callTrafficService(trafficRequest)
      emitt("trafficResponse.route=%s".format(trafficResponse))
      latch.countDown()
      emitt("trafficResponse.route = %s".format(trafficResponse))
    }
  }

  def testGetRoute = {
    val latch = new CountDownLatch(1)

    exampleHandlingEventResult(latch).map { route =>
      emitt("route is %s".format(route))
      latch.countDown()
    }
    latch.await()
  }

  def testForEach = {
    val latch: CountDownLatch = new CountDownLatch(1)
    exampleHandlingEventResultForEach(latch)
    latch.await()
  }

  def callTest1 {
    def testCallBothDirectly(ticketNr: Long) = {
      val request = EventRequest(ticketNr)

      val futureRoute: Future[Route] = Future {
        callEventService(request).event
      }.map { event =>
        val trafficRequest = TrafficRequest(
          destination = event.location,
          arrivalTime = event.time)
        callTrafficService(trafficRequest).route
      }

      futureRoute
    }

    val latch: CountDownLatch = new CountDownLatch(1)
    testCallBothDirectly(10).foreach { route =>
      emitt("got a route:%s".format(route))
      latch.countDown()
    }
    latch.await()
  }

  def getEvent(ticketNr: Long): Future[Event] = {
    val request = EventRequest(ticketNr)
    Future {
      callEventService(request).event
    }
  }
  def getRoute(event: Event): Future[Route] = {
    val f = Future {
      val trafficRequest = TrafficRequest(
        destination = event.location,
        arrivalTime = event.time)
      callTrafficService(trafficRequest).route
    }
    f
  }
  def callTest2 {
    /**
     * The concept to note here is this:
     * The fact that getEvent returns a Future[Event] and
     * along with the fact that getRoute wants an Event argument
     * requires that we use flatMap instead of map in this 
     * example. flatMap will retrieve the Event from the Future and 
     * pass it along to getRoute which will then return a Future[Route]
     * Similarly, the final foreach will unwinde the final Future 
     * from the getRoute call when the Future completes.
     * Note that the use of the latch here is to control the example
     * in a meaningful manner; otherwise the main program would simply 
     * exit before anything happened. 
     */
    val latch = new CountDownLatch(1)
    val fin = getEvent(10).flatMap {
//    val fin = getEvent(10).map {

      event => {
        emitt("event = %s".format(event))
        getRoute(event)
      }

    }.foreach {
      route =>
        emit("final route is %s".format(route))
        latch.countDown()
    }
    latch.await()
  }

  callTest2

}