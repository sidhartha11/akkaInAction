package org.geo.akkainaction.chapterfutures.illustrations

import org.geo.akkainaction.chapterfutures.entities.CaseEntities._
import org.geo.akkainaction.chapterfutures.UtilityFunctions._
import org.joda.time.DateTime

object SimpleExampleFunctions {
  
  def callEventService(request: EventRequest): EventResponse = {
    emitt("entering callEventService")
    val (lat,lon) = latAndLon
    waitSim(5000)
    val s = "event:" + randomString(10)
    val event: Event = Event(s,Location(lat,lon), new DateTime())
    val f = EventResponse(request.ticketNr, event)
    emitt("leaving callEventService:" + s)
    f
  }
  
  //   case class TrafficResponse(destination: String, route: Route, time: DateTime)

  def callTrafficService(trafficRequest: TrafficRequest) = {
    val routeI = randRange(1, 3)
    val routeS = routeI match {
      case 1 => "stormy weather route"
      case 2 => "good weather route"
      case 3 => "optimal route"
    }
    val destination = routeI match {
      case 1 => "bob dylyn"
      case 2 => "George Benson"
      case 3 => "John Coltrane"
    }
    
    TrafficResponse(destination, Route(routeS),new DateTime())
  }
  
}