package org.geo.channels.eventstream

/**
 * Simple case class for use in akka-in-action Message Channels chapter
 */
case class Order(customerId: String, productId: String, number: Int)
