package org.geo.aia.fsm

import akka.actor.{FSM
  ,Actor
  ,ActorRef
  ,ActorLogging
  ,Props
  ,ActorSystem}

import akka.actor.Actor

case object PendingRequests
case class BookRequest(context: AnyRef, target: ActorRef)
case class BookSupply(nrBooks: Int)
case object BookSupplySoldOut
case object Done

//responses
case object PublisherRequest
case class BookReply(context: AnyRef, reserveId: Either[String, Int])

sealed trait State


/**
 * The way to set up the states associated with the FSM is accomplished 
 * by extending a trait that is later used in the FSM.
 * Hence, State is the super type of all states used in the model. 
 * The FSM also requires a type of data that is tracked. Here we use StateData
 * as that type. 
 */
case object WaitForRequests extends State 
case object ProcessRequest extends State 
case object WaitForPublisher extends State 
case object SoldOut extends State 
case object ProcessSoldOut extends State

/** state data **/
case class StateData(nrBooksInStore: Int,
    pendingRequests: Seq[BookRequest])
 /**
  * We declare our FSM by extending both Actor and the FSM trait which is 
  * parameterized by our State and StateData types. 
  */
 class Inventory(publisher: ActorRef) extends Actor with FSM[State,StateData] {
  
  var reserveId = 0
  /**
   * The initial state is set with the startWith method. We start 
   * with WaitForRequests state with an empty StateData.
   */
  startWith(WaitForRequests, new StateData(0,Seq()))
  
  /**
   * WaitForRequests State
   */
  when(WaitForRequests) {
    case Event(request:BookRequest, data:StateData) => {
      val newStateData = data.copy(
          pendingRequests = data.pendingRequests :+ request)
      if ( newStateData.nrBooksInStore > 0) {
        goto(ProcessRequest) using newStateData 
      } else {
        goto(WaitForPublisher) using newStateData
      }
    }
    
    case Event(PendingRequests, data:StateData) => {
      if ( data.pendingRequests.isEmpty) {
        stay
      } else if (data.nrBooksInStore > 0) {
        goto(ProcessRequest)
      } else {
        goto(WaitForPublisher)
      }
    }
  } /** end When(WaitForRequests) **/
  
  /**
   * WaitForPublisher
   */
  when(WaitForPublisher) {
    case Event(supply:BookSupply, data:StateData) => {
      goto(ProcessRequest) using data.copy(
          nrBooksInStore = supply.nrBooks)
    }
    case Event(BookSupplySoldOut, _) => {
      goto(ProcessSoldOut)
    }
  }
  /**
   * ProcessRequest
   */
  when(ProcessRequest) {
    case Event(Done, data:StateData) => {
      goto(WaitForRequests) using data.copy(
          nrBooksInStore = data.nrBooksInStore -1,
          pendingRequests = data.pendingRequests.tail)
    }
  }
  /**
   * SoldOut
   */
  when(SoldOut) {
    case Event(request:BookRequest, data:StateData) => {
      goto(ProcessSoldOut) using new StateData(0,Seq(request))
    }
  }
  /**
   * ProcessSoldOut
   */
  when(ProcessSoldOut) {
    case Event(Done, data:StateData) => {
      goto(SoldOut) using new StateData(0,Seq())
    }
  }
  /**
   * Default Behaviour 
   */
  whenUnhandled {
    // common code for all sates 
    case Event(request:BookRequest, data:StateData) => {
      stay using data.copy(
          pendingRequests = data.pendingRequests :+ request)
    }
    case Event(e,s) => {
      log.warning("received unhandled request {} in state {}/{}",
          e, stateName, s)
          stay
    }
  }  /** end of whenUnhandled **/
  
  /**
   * INITIALIZE SECTION
   */
  initialize
  /**
   * Transition Section
   */
  onTransition {
    case _ -> WaitForRequests => {
      if (!nextStateData.pendingRequests.isEmpty) {
        // go to next state 
        self ! PendingRequests 
      }
    }
    
    case _ -> WaitForPublisher => {
      // send request to publisher
      publisher ! PublisherRequest
    }
    
    case _ -> ProcessRequest => {
      val request = nextStateData.pendingRequests.head
      reserveId += 1 
      request.target ! new BookReply(request.context,Right(reserveId))
      self ! Done 
    }
    
    case _ -> ProcessSoldOut => {
      nextStateData.pendingRequests.foreach( request => {
        request.target ! new BookReply(request.context, Left("SoldOut"))
      })
      self ! Done 
    }
  }
  
}