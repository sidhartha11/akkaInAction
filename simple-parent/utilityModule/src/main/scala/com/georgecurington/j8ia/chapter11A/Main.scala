package com.georgecurington.j8ia.chapter11A

import scala.concurrent.Future
// import scala.concurrent.ExecutionContext.Implicits.global

import Utils._
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

import Quote._
import Discount._
object Main extends App {
  val shops: List[ShopperApiIntf] =
    List(
      new Shop("BestPrice"),
      new Shop("LetsSaveBig"),
      new Shop("MyFavoriteShop"),
      new Shop("BuyItAll"),
      new Shop("AMC")
    //        new Shop("BestPrice2"),
    //        new Shop("LetsSaveBig2"),
    //        new Shop("MyFavoriteShop2"),
    //        new Shop("BuyItAll2"),
    //        new Shop("AMC2"),
    //        new Shop("BestPrice"),
    //        new Shop("LetsSaveBig"),
    //        new Shop("MyFavoriteShop"),
    //        new Shop("BuyItAll"),
    //        new Shop("AMC"),
    //        new Shop("BestPrice2"),
    //        new Shop("LetsSaveBig2"),
    //        new Shop("MyFavoriteShop2"),
    //        new Shop("BuyItAll2"),
    //        new Shop("AMC2"),
    //        new Shop("BestPrice"),
    //        new Shop("LetsSaveBig"),
    //        new Shop("MyFavoriteShop"),
    //        new Shop("BuyItAll"),
    //        new Shop("AMC"),
    //        new Shop("BestPrice2"),
    //        new Shop("LetsSaveBig2"),
    //        new Shop("MyFavoriteShop2"),
    //        new Shop("BuyItAll2"),
    //        new Shop("AMC2"),
    //        new Shop("BestPrice"),
    //        new Shop("LetsSaveBig"),
    //        new Shop("MyFavoriteShop"),
    //        new Shop("BuyItAll"),
    //        new Shop("AMC"),
    //        new Shop("BestPrice2"),
    //        new Shop("LetsSaveBig2"),
    //        new Shop("MyFavoriteShop2"),
    //        new Shop("BuyItAll2"),
    //        new Shop("AMC2")
    );

  implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(shops.size);

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }

  def findPricesNonParallel(product: String): List[String] = {
    for (l <- shops) yield {
      "%s price is %.2f".format(l.getName, l.getPrice(product))
    }
  }
  //		return shops.stream()
  //				.map(shop -> shop.getPrice(product))
  //				.map(Quote::parse)
  //				.map(Discount::applyDiscount)
  //				.collect(toList());

  def findPrices(product: String): List[Future[String]] = {
    
//    val f1 = shops.map(_.getPriceAsyncString(product))
//    val f2 =  Future.sequence(f1)
//    f2.flatMap(p => parseFuture(p))
//    f2.flatMap( p => parseFuture(p))
////    
////      .flatMap(p => parseFuture(p))
////      .map(p => applyDiscount(p))
    ???
  }
  
    def findPricesSerial(product: String): List[String] = {
    shops
      .map(_.getPrice(product))
      .map(p => parse(p))
      .map(p => applyDiscount(p))
  }

  def findPricesWithgetPriceAsync(product: String): List[Future[Double]] = {
    shops.map(
      _.getPriceAsync(product))
  }

  def findPricesParallelParMap(product: String): List[String] = {
    shops.par.map(
      l =>
        "%s price is %.2f".format(l.getName, l.getPrice(product))).toList
  }

  def findPrices2(product: String): List[String] = {
    shops.par.map(
      l =>
        "%s price is %.2f".format(l.getName, l.getPrice(product))).toList
  }

  def test_2 = {
    val latch = new CountDownLatch(1)
    val shop: ShopperApiIntf = new Shop("BestShop")
    val start = System.nanoTime();
    val futurePrice = shop.getPriceAsync("Some Product")
    val invocationTime = ((System.nanoTime() - start) / 1000000)
    doSomethingElse
    peep("Invocation returned after %d msecs".format(invocationTime));
    futurePrice.onComplete {
      case Success(v) =>
        val retrievalTime = ((System.nanoTime() - start) / 1000000);
        peep("value return is:" + v)
        peep("Price returned after %d msecs".format(retrievalTime));
        latch.countDown()
      case Failure(e) =>
        peep("failed with exception:" + e)
    }

    latch.await()
  }

  def test_3 = {
    val start = System.nanoTime()
    peep(findPricesParallelParMap("myPhone27s"))
    val duration = ((System.nanoTime() - start) / 1000000)
    peep("Done in %d msecs".format(duration))
  }

  def test_1a(product: String) = {
    val start = System.nanoTime();
    val latch = new CountDownLatch(1)
    Future.sequence(shops.map(
      _.getPriceAsync(product))).onComplete {
      case Success(v) =>
        val retrievalTime = ((System.nanoTime() - start) / 1000000);
        peep("value return is:" + v)
        peep("Price returned after %d msecs".format(retrievalTime));
        latch.countDown()
      case Failure(e) =>
        throw e
    }
    latch.await()
  }

  def test_1b(product: String) = {
    Future.sequence(shops.map(
      _.getPriceAsync(product)))
  }

  def test_1 = {
    val latch = new CountDownLatch(1)
    val start = System.nanoTime();

    val futs = findPricesWithgetPriceAsync("myPhone27s")
    Future.sequence(futs).onComplete {
      case Success(v) =>
        val retrievalTime = ((System.nanoTime() - start) / 1000000);
        peep("value return is:" + v)
        peep("Price returned after %d msecs".format(retrievalTime));
        latch.countDown()
      case Failure(e) =>
        peep("failed with exception:" + e)
    }
    latch.await()

  }

  def calltest_1b() {
    val latch = new CountDownLatch(1)
    val start = System.nanoTime();
    test_1b("myPhone27s") foreach {
      list =>
        val retrievalTime = ((System.nanoTime() - start) / 1000000);
        println("the list is:" + list)
        println("Price returned after %d msecs".format(retrievalTime));

        latch.countDown()
    }
    latch.await()
  }
  def doSerial = {
     val start = System.nanoTime();

  peep(findPricesSerial("myPhone27s"))

  val duration = ((System.nanoTime() - start) / 1000000);
  peep("Done in %d msecs".format(duration))
  }
  
  val start = System.nanoTime();

//  peep(findPrices("myPhone27s"))

  val duration = ((System.nanoTime() - start) / 1000000);
  peep("Done in %d msecs".format(duration))

}