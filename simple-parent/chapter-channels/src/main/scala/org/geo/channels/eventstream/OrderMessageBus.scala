package org.geo.channels.eventstream

import akka.event.LookupClassification
import akka.event.EventBus
import akka.event.ActorEventBus

/**
 * Following method is used to create a custom EventBus.
 * Cake pattern employed here using Scala Traits.
 * By extending the Traits: LookupClassification and ActorEventBus
 * and then overriding the classify and publish methods along with 
 * implementing the abstract tpes, Event and Classifier. 
 */
class OrderMessageBus extends EventBus 
with LookupClassification  
with ActorEventBus {
  type Event = Order 
  type Classifier = Boolean 
  
  def mapSize = 2 
  
  protected def classify(event: OrderMessageBus#Event) = {
    event.number > 1 
  }
  
  protected def publish(event: OrderMessageBus#Event,
      subscriber: OrderMessageBus#Subscriber): Unit = {
    subscriber ! event
  }
}