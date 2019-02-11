package com.georgecurington.j8ia.chapter11

//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
//
//import Utils._
//import scala.util.Success
//import scala.util.Failure
//
//
//
//object CompareCompletableFuture {
// 
//  def main(args: Array[String]) {
//    
//    val shop:ShopperApiIntf = new Shop("BestShop")
//    val start = System.nanoTime();
//    val futurePrice = shop.getPriceAsync("Some Product")
//    val invocationTime = ((System.nanoTime() - start ) / 1000000)
//    doSomethingElse
//    peep("Invocation returned after %d msecs".format(invocationTime));		
//    futurePrice.onComplete{
//      case Success(v) =>   
//        peep("value return is:" + v)
//        val retrievalTime = (( System.nanoTime() - start) / 1000000);
//		    peep("Price returned after %d msecs".format(retrievalTime));
//
//      case Failure(e)   => 
//        peep("failed with exception:" + e)
//    }
//    
//  }
//}