package org.geo.aia.fsm

import akka.actor.{ Actor, ActorLogging }

class Publisher(totalNrBooks: Int, nrBooksPerRequest: Int) extends Actor with ActorLogging {

  var nrLeft = totalNrBooks
  def receive: Receive = {
    case PublisherRequest => {
      if (nrLeft == 0) {
        sender() ! BookSupplySoldOut
      } else {
        val supply = Math.min(nrBooksPerRequest, nrLeft)
        nrLeft -= supply
        sender() ! new BookSupply(supply)
      }
    }
  }
}

//object TestPub extends App {
//  def giveDhimantOriginal(prop1: String, prop2: String): Option[String] = {
//    if (prop1 + prop2 == "BILLIONDOLLARS")
//      Some("Dhimant Yyas gets Original")
//    else
//      None
//  }
//
//  val s = for (res <- giveDhimantOriginal("BILLION", "DOLLARS")) yield {
//    res
//  }
//  println(s)
//}