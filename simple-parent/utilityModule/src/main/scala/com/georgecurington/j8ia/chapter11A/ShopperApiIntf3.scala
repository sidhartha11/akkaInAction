package com.georgecurington.j8ia.chapter11A

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import Utils._
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


trait ShopperApiIntf {
  def getPrice(product: String): String
  def getPriceAsyncDouble(product: String): Future[Double]
    def getPriceAsync(product: String): Future[Double]

  def getPriceAsyncString(product: String): Future[String]

  def getName: String
}

class Shop(name: String) extends ShopperApiIntf {
  import Main.shops
  import Discount._
  
    implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(20);

    def execute(runnable: Runnable) {
        threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
}
  def getPriceAsyncString(product: String): Future[String] = {
    Future {
   /** get the double price **/
    val price = calculatePrice(product)
    /** get the DiscountType **/
    val discountCode = randomDiscount()
    "%s:%.2f:%s".format(name,price,discountCode.codeName)
    }
  }
  
    def getPriceAsyncDouble(product: String): Future[Double] = {
    Future {
//     peep("calling calculatePrice")
      val f = calculatePrice(product)
//      peep("f = " + f)
      f
    }
  }
    
    def getPriceAsync(product: String): Future[Double] = {
    Future {
//     peep("calling calculatePrice")
      val f = calculatePrice(product)
//      peep("f = " + f)
      f
    }
  }

 /**
  * This get the length of the values of the enum 
  * Discount.Code ... then creates a random value to index
  * into the array of Discount.Code to pick one.
  * then converts to a string name and creates the resulting
  * string. 	
  */
//		Discount.Code code = Discount.Code.values()[RANDOM.nextInt(Discount.Code.values().length)];
//		return String.format("%s:%.2f:%s", name,price,code.name());
  def getName: String = name

  def getPrice(product: String): String = {
    /** get the double price **/
    val price = calculatePrice(product)
    /** get the DiscountType **/
    val discountCode = randomDiscount()
    "%s:%.2f:%s".format(name,price,discountCode.codeName)
  }
  
    def getPriceString(product: String): String = {
    /** get the double price **/
    val price = calculatePrice(product)
    /** get the DiscountType **/
    val discountCode = randomDiscount()
    "%s:%.2f:%s".format(name,price,discountCode.codeName)
  }
  
    def getPriceDouble(product: String): Double = {
    calculatePrice(product)
  }

  private def calculatePrice(product: String): Double = {
      delay()
      val price = getDValue(product)
//    peep("calculated price %f for %s".format(price, product))
    return price
  }
}