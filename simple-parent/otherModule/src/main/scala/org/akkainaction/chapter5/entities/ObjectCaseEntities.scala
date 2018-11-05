package org.akkainaction.chapter5.entities
import java.util.{Date}

object ObjectCaseEntities {
  
case class Route(route: String)
case class EventRequest(id: Long) 
case class Event(body: String)
case class EventResponse(id: Int
    , event: String
    , location: String
    , timeStamp: String)
  

case class TrafficRequest(id: Long, destination: String, arrivalTime: String)
case class TrafficResponse(id: Long, destination: String, route: String, arrivalTime: String)

case class TicketInfo(ticketNr: Long,
    event: Option[Event]=None,
    route: Option[Route]=None )
}