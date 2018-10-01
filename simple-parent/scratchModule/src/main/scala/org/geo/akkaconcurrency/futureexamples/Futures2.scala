package org.geo.akkaconcurrency.futureexamples

import scala.concurrent.{Await, Future, future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Futures2 {
      implicit val baseTime = System.currentTimeMillis

      def longRunningComputation(i: Int): Future[Int] = 
        future {
        sleep(100)
        i + 1
      }
  def main(args: Array[String]) : Unit = {
    
   
  }
  
}