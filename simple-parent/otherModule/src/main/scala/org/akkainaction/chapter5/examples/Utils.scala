package org.akkainaction.chapter5.examples

import scala.concurrent.Future
import org.akkainaction.chapter5.entities.EventResponse
import org.akkainaction.chapter5.entities.EventRequest

object Utils {
  
  def callEventService(request: EventRequest): EventResponse = {
    println("synchronously calling EventService")
    null
  }
}