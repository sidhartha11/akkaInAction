package com.georgecurington.j8ia.chapter11A

import Utils._
import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.Future
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class Quote(shopName: String, price: Double, discountCode: Discount.DiscountType) {
  import Discount._

  def getShopName = shopName
  def getPrice = price
  def getDiscountCode = discountCode

}

object Quote {
  import Discount._
  
    implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(20);

    def execute(runnable: Runnable) {
        threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
}
  def parse(s: String): Quote = {
    val split = s.split(":")
    val shopName = split(0)
    val price = split(1).toDouble
    println("parse(s) = " + s)
    val discountCodePar: String => DiscountType = {
      case "NONE"     => NONE()
      case "SILVER"   => SILVER()
      case "GOLD"     => GOLD()
      case "PLATINUM" => PLATINUM()
      case "DIAMOND"  => DIAMOND()
    }
    val discountCode = discountCodePar(split(2))
    new Quote(shopName, price, discountCode)
  }
  
    def parseFuture(s: String): Future[Quote] = {
    Future {
    val split = s.split(":")
    val shopName = split(0)
    val price = split(1).toDouble
    println("parse(s) = " + s)
    val discountCodePar: String => DiscountType = {
      case "NONE"     => NONE()
      case "SILVER"   => SILVER()
      case "GOLD"     => GOLD()
      case "PLATINUM" => PLATINUM()
      case "DIAMOND"  => DIAMOND()
    }
    val discountCode = discountCodePar(split(2))
    new Quote(shopName, price, discountCode)
    }
  }
}

object Discount {

  trait DiscountType {
    val percentage: Int
    val codeName: String
  }
  case class NONE(name: String = "NONE") extends DiscountType {
    val percentage: Int = 0
    val codeName = name
  }
  case class SILVER(name: String = "SILVER") extends DiscountType {
    val percentage: Int = 5
    val codeName = name

  }
  case class GOLD(name: String = "GOLD") extends DiscountType {
    val percentage: Int = 10
    val codeName = name

  }
  case class PLATINUM(name: String = "PLATINUM") extends DiscountType {
    val percentage: Int = 15
    val codeName = name

  }
  case class DIAMOND(name: String = "DIAMOND") extends DiscountType {
    val percentage: Int = 20
    val codeName = name

  }

  def applyDiscount(quote: Quote): String = {
    quote.getShopName + " price is " + apply(quote.getPrice, quote.getDiscountCode)
  }

  def apply(price: Double, code: DiscountType): Double = {
    delay()
    format(price * (100 - code.percentage) / 100)
  }

  def randomDiscount(): DiscountType = {
    ThreadLocalRandom.current().nextInt(5) match {
      case 0 =>
        NONE()
      case 1 =>
        SILVER()
      case 2 =>
        GOLD()
      case 3 =>
        PLATINUM()
      case 4 =>
        DIAMOND()
    }
  }

}
