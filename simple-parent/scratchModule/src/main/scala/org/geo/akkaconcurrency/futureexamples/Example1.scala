package org.geo.akkaconcurrency.futureexamples

import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scala.util.{Failure , Success}

object Example1 {
  def main(args: Array[String]): Unit = {
    println("starting calculation ...")
    val f = Future {
      sleep(Random.nextInt(500))
      42/0
    }
    println("before onComplete")
    f.onComplete {
      case Success(value) => 
        println("Got the callback,meaning = %s".format(value))
      case Failure(e) => println("Got Failure:%s".format(e))
    }
    
    // do the rest of your work
    println("A ..."); sleep (100) 
    println("B ..."); sleep (100)
    println("C ..."); sleep(100)
    println("D ..."); sleep(100)
    println("E ..."); sleep(100)
    println("F ..."); sleep(100)
    sleep(2000)
  }
}