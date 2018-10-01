package org.geo.akkaconcurrency.futureexamples

import java.lang.Thread._

/** import the future methods and classes **/
import scala.concurrent.{ Await, Future }
/** import the time methods and classes **/
import scala.concurrent.duration._
/** import the implicits.global definitions **/
/** this is the default global execution context **/
/** think of the execution context as a Thread pool **/
import scala.concurrent.ExecutionContext.Implicits.global

object Futures1 {

  def main(args: Array[String]) : Unit = {
    implicit val baseTime = System.currentTimeMillis

    // 2 - create a Future
    val f = Future {
      sleep(3000)
      println("future is running")
      1 + 1
    }

    // 3 - this is blocking (blocking is bad)
    println("about to execute Await.result(future,time)")
    val result = Await.result(f, 1 second)
    println("result = %s".format(result))
  }

}