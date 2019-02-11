package com.georgecurington.misc.examples

object Partials extends App{
  
  val bob = "bob"
  val testPartial: String => Unit = {
    case `bob` => println(bob)
    case _ => println("unknown")
  }
  testPartial("jonh")
}