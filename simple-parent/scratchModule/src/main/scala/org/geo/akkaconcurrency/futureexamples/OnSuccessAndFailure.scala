package org.geo.akkaconcurrency.futureexamples

import scala.concurrent.{Future}
import scala.util.{Failure,Success,Random}
import scala.concurrent.ExecutionContext.Implicits.global 

object OnSuccessAndFailure {
  
  def main(args: Array[String]): Unit = {
    
    val f = Future {
      sleep(Random.nextInt(500))
      val r = Random.nextInt(500)
      if ( r > 250) {
        println("r = %s".format(r))
        throw new Exception("Yies!")
      }
      else 
        r 
    }
    
    f.foreach(p => println("got %s".format(p)))
// onSuccess and onFailure are both deprecated
//    f onSuccess {
//      case result => println(s"Success: $result")
//    }
//    
//    f onFailure {
//      case t => println(s"Exception: ${t.getMessage}")
//    }
    
    // do the rest of your work
    println("A ..."); sleep(100)
    println("B ..."); sleep(100)
    println("C ..."); sleep(100)
    println("D ..."); sleep(100)
    println("E ..."); sleep(100)
    println("F ..."); sleep(100)
    sleep(2000)
    
  }
}