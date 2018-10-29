package org.akkainaction.chapter5.entities

case class Event(body: String)
case class EventResponse(id: Int, event: String) {
  
}