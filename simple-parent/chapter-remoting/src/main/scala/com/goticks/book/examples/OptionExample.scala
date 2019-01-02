package com.goticks.book.examples
case class Ticket(id: Int)
object OptionExample extends App {
  def tester() = 
  {
    println("executing tester")
    "rasberries"
  }
//  val t:Option[String] = Some("blue")
  val t:Option[String] = None

  
  t.fold(tester())( ele => {
    println("ele = %s".format(ele))
    ele
  }
  )
  
  
        val newTickets = (1 to 10).map { ticketId =>
          Ticket(ticketId)
        }// .toVector
}