package com.goticks

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor._
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import com.goticks.utilities.Utils._


trait RestApi extends BoxOfficeApi
    with EventMarshalling {
  import StatusCodes._

  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute
  val ents = "events"

  def eventsRoute =
    pathPrefix(ents) {
      pathEndOrSingleSlash {
        get {
          // GET /events
          emitt("1 get called")
          onSuccess(
              getEvents()
          ) { events =>
            emitt("event gotton = %s".format(events))
            complete(OK, events)
          }
          
        }
      }
    }
  /**
   * This section will create a new event with a payload of 
   * # of tickets
   */
  def eventRoute =
    pathPrefix("events" / Segment) { event =>
      pathEndOrSingleSlash {
        post {
          // POST /events/:event
          emitt("2 post called, event = " + event ,true)
          entity(as[EventDescription]) { ed =>
           emitt("3 ed = " + ed)
           /** here the actual ticket is created **/
           /** onSuccess is known as a directive **/
            onSuccess(createEvent(event, ed.tickets)) {
              case BoxOffice.EventCreated(event) => 
                emitt("4 event = " + event )
                val c = complete(Created, event)
                emitt("5 c = " + c)
                c
              case BoxOffice.EventExists =>
                emitt("6 duplicate event detected")
                val err = Error(s"$event event exists already.")
                complete(BadRequest, err)
            }
          }
        } ~
        get {
          // GET /events/:event
          emitt("7 get called:event=%s".format(event))

//          onSuccess(getEvent(event)) {
//            _.fold(complete(NotFound))(e => complete(OK, e))
//          }
          onSuccess(getEvent(event)) { ev => {
            emitt("ev in getEvent(event):%s".format(ev))
            ev.fold(complete(NotFound))(e => complete(OK, e))
          }
          }        
          
        } ~
        delete {
          // DELETE /events/:event
          emit("8 delete called",true)          
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }

  def ticketsRoute =
    pathPrefix("events" / Segment / "tickets") { event =>
      post {
        pathEndOrSingleSlash {
          // POST /events/:event/tickets
          emit("9 post called",true)
          entity(as[TicketRequest]) { request =>
            onSuccess(requestTickets(event, request.tickets)) { tickets =>
              if(tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, tickets)
            }
          }
        }
      }
    }
}

trait BoxOfficeApi {
  import BoxOffice._
  def log: LoggingAdapter
  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  emitt("10 inside trait BoxOfficeApi")
  emitt("11 calling createBoxOffice")
  /**
   * It seems that unless you force the box office to be actually set up
   * here, you are later forced to make a bogus call to force the lazy evaluation.
   * lazy val boxOffice = createBoxOffice
   */
//  lazy val boxOffice = createBoxOffice()
  val boxOffice = createBoxOffice()


  def createEvent(event: String, nrOfTickets: Int) = {
    emitt("12 inside createEvent calling boxOffice")

    log.info(s"Received new event $event, sending to $boxOffice")
    /** get the return value from the actor and store in a variable **/
    val fromRemote = boxOffice.ask(CreateEvent(event,nrOfTickets))
    emitt("13 fromRemote = %s".format(fromRemote))
    /** note that the value returned is a Future[Any] **/
    /** lets execute the mapTo function on the returned Future **/
    val fromRemoteMapTo = fromRemote.mapTo[EventResponse]
    emitt("14 fromRemoteMapTo = %s".format(fromRemoteMapTo))
    fromRemoteMapTo 
//    boxOffice.ask(CreateEvent(event, nrOfTickets))
//      .mapTo[EventResponse]
  }

  def getEvents() = {
    val r = boxOffice.ask(GetEvents).mapTo[Events]
    emitt("return value from  boxOffice.ask(GetEvents):%s".format(r))
    r
  }

  def getEvent(event: String) = {
    emitt("enter getEvent with event=%s".format(event))
    emitt("asking boxOffice")
    var fromboxOffice = boxOffice.ask(GetEvent(event))
    emitt("fromboxOffice = %s".format(fromboxOffice))
    /**
     * def mapTo[S](implicit tag: ClassTag[S]): Future[S]
     */
    fromboxOffice.mapTo[Option[Event]]
  }

  def cancelEvent(event: String) =
    boxOffice.ask(CancelEvent(event))
      .mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int) =
    boxOffice.ask(GetTickets(event, tickets))
      .mapTo[TicketSeller.Tickets]
}