package com.goticks

import scala.concurrent.Future

import akka.actor._

import akka.util.Timeout

import com.goticks.utilities.Utils._


object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"

  case class CreateEvent(name: String, tickets: Int)
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTickets(event: String, tickets: Int)
  case class CancelEvent(name: String)

  case class Event(name: String, tickets: Int)
  case class Events(events: Vector[Event])

  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse
}

class BoxOffice(implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  import context._

  def createTicketSeller(name: String) = {
    emitt("inside createTicketSeller")
    val tactor = context.actorOf(TicketSeller.props(name), name)
    emitt("created actor:%s:%s".format(tactor, tactor.getClass))
    tactor
  }

  def receive = {
    case CreateEvent(name, tickets) =>
      emitt("1 BoxOffice recieve:name=%s, tickets = %d".format(name,tickets))
      def create() = {
        /** create a ticket seller actor **/
        val eventTickets = createTicketSeller(name)
        emitt("about to create tickets ranging up to %d".format(tickets))
        /**
         * This section creates a vector of tickets.
         */
        val newTickets = (1 to tickets).map { ticketId =>
          TicketSeller.Ticket(ticketId)
        }.toVector
        emitt("newTickets created = %s".format(newTickets))
        emitt("sending newTickets to TicketSeller Actor")
        /**
         * This section sends a list of tickets in an Add message 
         * to the eventTickets Actor. It seems that everytime you call 
         * this function, a new Ticket Seller Actor will be created. 
         * And a new list of tickets will simply be created???
         */
        eventTickets ! TicketSeller.Add(newTickets)
        emitt("sending response to sender()")
        /**
         * Now just send an EventCreated message containing the name and tickes
         * back to some originator , in this case, the proxy actor.
         */
        sender() ! EventCreated(Event(name, tickets))
      }
      emitt("calling part that indicates EventExists")
      val currentChild = context.child(name) 
      emitt("current child is %s".format(currentChild))
 //     context.child(name).fold(create())(_ => sender() ! EventExists)
      currentChild.fold(create())(ele => {
        emitt("folding in %s".format(ele))
        sender() ! EventExists
      }
      )


    case GetTickets(event, tickets) =>
      def notFound() = sender() ! TicketSeller.Tickets(event)
      def buy(child: ActorRef) =
        child.forward(TicketSeller.Buy(tickets))

      context.child(event).fold(notFound())(buy)

    case GetEvent(event) =>
      def notFound() = sender() ! None
      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent
      context.child(event).fold(notFound())(getEvent)

    case GetEvents =>
      import akka.pattern.{ ask, pipe }

      def getEvents = {
        /** first get a list of all actors created thus far **/
        val progeny = context.children
        emitt("children are %s".format(progeny))
 //       context.children.map { child => {
        /**
         * now map each child as follows:
         * send a GetEvent(child's name) message to this actor 
         * and mapped the result to an Option[Event] 
         * This will result is an Iterable[Future[Option[BoxOffice.Event]]]
         * Note that each repitition will return a Future[Option[BoxOffice.Event]] 
         * The totality of the calls will return a list of these, i.e. Iterable
         * 
         */
        val r = progeny.map { child => {

      
        emitt("child in getEvents is %s".format(child))
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }
      }
      r
  }
      def convertToEvents(f: Future[Iterable[Option[Event]]]) = {
        /**
         * This function must take the Iterables and flatten them into a list of Event 
         * objects.
         * Iterable[Option[Event]]  
         */
        f.map(_.flatten).map(l=> Events(l.toVector))
      }

      /** first get a List Of Futures **/
      // val listOfFutures: Iterable[Future[Option[BoxOffice.Event]]]
      val listOfFutures = getEvents
      /** now convert to a Future Of Lists **/
      // val futureOfLists: Future[Iterable[Option[BoxOffice.Event]]]
      val futureOfLists = Future.sequence(listOfFutures)
      /** now convert to Events **/
      // val convertedToEvents: Future[BoxOffice.Events]
      val convertedToEvents  = convertToEvents(futureOfLists)
 //     val convertedToFutureOfList = Future.sequence(getEvents)
      
      /** now pipe the completed Events to the sender() **/
      pipe(convertedToEvents) to sender()

    case CancelEvent(event) =>
      def notFound() = sender() ! None
      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel
      context.child(event).fold(notFound())(cancelEvent)
  }
}

