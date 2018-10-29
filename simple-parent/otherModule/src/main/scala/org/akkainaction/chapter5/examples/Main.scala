package org.akkainaction.chapter5.examples

import org.akkainaction.chapter5.entities.EventResponse

import org.akkainaction.chapter5.entities.EventRequest

object Main extends App {
  import Utils._
  val request = EventRequest(100)
  println("created request:%s".format(request))
  val response: EventResponse = callEventService(request)
}