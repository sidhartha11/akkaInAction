package org.geo.akkainaction.chapterfutures.entities

import org.joda.time.DateTime

object CaseEntities {
   case class EventRequest(ticketNr: Long)
  
  case class Event(name: String 
      , location: Location
      , time: DateTime
      )
  
  case class Location(lat: Double, lon:Double)
  
  case class EventResponse(ticketNr: Long, event: Event)
  
  
  
  case class Route(route: String)
  case class TrafficResponse(destination: String, route: Route, time: DateTime)
  case class TrafficRequest(destination: Location, arrivalTime:DateTime)

  case class TicketInfo(ticketNr: String,
      event:Option[Event] = None,
      route:Option[Route] = None )
}