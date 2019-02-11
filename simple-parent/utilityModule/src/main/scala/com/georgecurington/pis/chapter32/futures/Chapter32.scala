package com.georgecurington.pis.chapter32.futures

import scala.concurrent.{Future,Await}
import scala.util.{Success , Failure}
import scala.concurrent.duration._
import scala.util.Try

object Chapter32 extends App {
  import Utils._

  def section32_1_1 {
    glock(1)
    val fut = Future {
      Thread.sleep(1000)
      rlock()
      21 + 21
    }
    hlock()
    peep(fut)
    stop
  }

  def section32_1_2 {
    glock(1)
    val fut = Future {
      Thread.sleep(5000)
      val r = 21 + 21
      rlock()
      r
    }
    peep("fut.isCompleted=%b".format(fut.isCompleted))

    /**
     * def value: Option[Try[Int]]
     * The current value of this Future.
     * Note: using this method yields nondeterministic dataflow programs.
     * If the future was not completed the returned value will be None.
     * If the future was completed the value will be Some(Success(t))
     * if it contained a valid result, or Some(Failure(error))
     * if it contained an exception.
     * Returns
     * None if the Future wasn't completed, Some if it was.
     *
     */
    peep("fut.value = %s".format(fut.value))
    hlock()
    Thread.sleep(250)
    peep(fut)
    peep("fut.value = %s".format(fut.value))
    stop
  }

  def section32_2_1 {
    glock(1)
    val fut = Future {
      Thread.sleep(5000)
      val r = 21 / 0
      rlock()
      r
    }
    peep("fut.isCompleted=%b".format(fut.isCompleted))

    /**
     * def value: Option[Try[Int]]
     * The current value of this Future.
     * Note: using this method yields nondeterministic dataflow programs.
     * If the future was not completed the returned value will be None.
     * If the future was completed the value will be Some(Success(t))
     * if it contained a valid result, or Some(Failure(error))
     * if it contained an exception.
     * Returns
     * None if the Future wasn't completed, Some if it was.
     *
     */
    peep("fut.value = %s".format(fut.value))
    hlock()
    Thread.sleep(250)
    peep(fut)
    peep("fut.value = %s".format(fut.value))
    stop
  }

  def section32_1 {
    glock(1)
    val fut = Future {
      w(5000)
      21 + 21
    }
    val result = fut.map {
      t =>
        rlock
        t + 1
    }
    /**
     * Note:
     * result holds a Future object whose value is an Option of type
     * Try or None. The Try holds a Success(val) or Failure(exception)
     * Option[scala.util.Try[Int]] = Some(Success(43))
     * Option can be a Try  .. or .. None
     * The Try can be a Success(val) or Failure(Throwable)
     */
    peep("result = %s, result.value=%s".format(result, result.value))
    hlock
    peep("result = %s, result.value=%s".format(result, result.value))
  }

  def section32_3 {
    val start = System.currentTimeMillis()
    glock(1)
    val fut1 = Future {
      w(10000)
      21 + 21
    }

    val fut2 = Future {
      w(10000)
      23 + 23
    }

    val result = for {
      x <- fut1
      y <- fut2
    } yield {

      val end = (System.currentTimeMillis() - start) / 1000
      peep("elapsed = %d".format(end))
      rlock
      x + y
    }
    peep("result = %s , result.value = %s".format(result, result.value))
    hlock
    peep("result = %s, result.value=%s".format(result, result.value))
    /**
     * final out is
     * result = Future(<not completed>) , result.value = None
     * result = Future(Success(88)), result.value=Some(Success(88))
     * Note: since the Futures are create prior to executing the for Expression
     * which returns another future, they all run in parallel and should take
     * around 10 seconds. The example, section32_3_1 takes 20 seconds because
     * for expressions serialize their processing
     *
     */
    stop
  }

  def section32_3_1 {
    val start = System.currentTimeMillis()
    glock(1)

    val result = for {
      x <- Future { w(5000); 21 + 21 }
      y <- Future { w(5000); 23 + 23 }
    } yield {

      val end = (System.currentTimeMillis() - start) / 1000
      peep("elapsed = %d".format(end))
      rlock
      x + y
    }
    peep("result = %s , result.value = %s".format(result, result.value))
    hlock
    peep("result = %s, result.value=%s".format(result, result.value))
    /**
     * final out is
     * result = Future(<not completed>) , result.value = None
     * result = Future(Success(88)), result.value=Some(Success(88))
     * Note: since the Futures are create prior to executing the for Expression
     * which returns another future, they all run in parallel and should take
     * around 10 seconds. The example, section32_3_1 takes 20 seconds because
     * for expressions serialize their processing
     *
     */
    stop
  }
  
    def section32_3_2 {
    val start = System.currentTimeMillis()
    glock(1)

    /**
     * Note:
     * Future.successful just seems to run the Future synchronously 
     */
    val result =  Future.successful {
      w(5000)
      val end = (System.currentTimeMillis() - start) / 1000
      peep("elapsed = %d".format(end))
      rlock
      21 + 21 
    }
    peep("result = %s , result.value = %s".format(result, result.value))
    hlock
    peep("result = %s, result.value=%s".format(result, result.value))
    stop
  }
    
      def section32_3_3 {
    val start = System.currentTimeMillis()
    glock(1)

    /**
     * Note:
     * Future.successful just seems to run the Future synchronously 
     */
    val result =  Future.failed {
      w(5000)
      val end = (System.currentTimeMillis() - start) / 1000
      peep("elapsed = %d".format(end))
      rlock
      21 + 21 
      throw new Exception("Bummer!")
    }
    peep("result = %s , result.value = %s".format(result, result.value))
    hlock
    peep("result = %s, result.value=%s".format(result, result.value))
    stop
  }
  
  def section32_3_4 {
    val fut = Future { 42 }
    val valid = fut.filter { 
      res => 
        peep("res = %s".format(res))
        res < 0 
    }
    
    Thread.sleep(3000)
    peep("valid = %s, valid.value = %s".format(valid, valid.value))
  }
  
  def section32_3_5 {
    val failure = Future { 42 / 0 }
    peep("failure = %s , failure.value = %s".format(failure, failure.value))
    val expectedFailure = failure.failed
    peep("expectedFailure = %s, expectedFailure.value = %s".format(expectedFailure, expectedFailure.value))
    hang
  }
  
  def section32_3_6 {
    val failure = Future { 42 / 1 }
    peep("failure = %s , failure.value = %s".format(failure, failure.value))
   val expectedFailure = failure.failed
    peep("expectedFailure = %s, expectedFailure.value = %s".format(expectedFailure, expectedFailure.value))
    hang
  }
  
  def section32_3_7 {
    val success = Future { 42 / 1 }
    val failure = Future { 42 / 0 }
    val fallback = failure.fallbackTo(success)
    w(2000)
    peep("fallback = %s , fallback.value = %s".format(fallback, fallback.value))
    hang
  }
  def section32_3_8 {
    val failure = Future { 42 / 0 }
    
    val failedFallback = failure.fallbackTo(
        Future {
          val res = 42 
          require(res < 0) 
          res
        }
        )
        
    w( 1000 )
    peep (
        "failedFallback = %s, failedFallback.value = %s".format(
            failedFallback , failedFallback.value))
  }
  def section32_3_9 {
       val failure = Future { 42 / 0 }
    
    val failedFallback = failure.fallbackTo(
        Future {
          val res = 42 
          require(res < 0) 
          res
        }
        )
        
     val recovered = failedFallback recover {
      case ex: ArithmeticException => -1
    }
    w(1000)
    peep("recovered = %s , recovered.value = %s".format(recovered, recovered.value))
  }
  
  def section32_3_10 {
    val failure = Future { 42 / 2 }
    val failedFallback = failure.fallbackTo (
        Future { 
          val res =  42 
          require (res > 0)
          res
        }
        )
     val recovered = failedFallback recover {
      case ex: ArithmeticException => -1 
    }
    
        w(1000)
    peep("recovered = %s , recovered.value = %s".format(recovered, recovered.value))
  }
  def section32_3_11 {
    val success = Future { 42/ 0 }
    val first = success.transform(
        res => res - 21 ,
        ex => new Exception("see cause",ex)
        )
        w(100)
        peep("first = %s, first.value = %s".format(first, first.value))
  }
  /**
   * Note:
   * Here the transform function takes two functional arguments.
   * The first one handles a successfull computation,
   * the second an exceptional result 
   */
  def section32_3_12 {
    val success = Future ( 42 + 1 )
    val first = success.transform (
        res => res * -1 ,
        ex => new Exception("see cause = %s".format(ex))
            )
        w(1000)
        peep ( "fist = %s , first.valu = %s".format(first, first.value))
  }
  /**
   * Overloaded version of transform introduced in scala 2.12
   * Here the function takes a partial function that maps a 
   * Try to a Try 
   */
  def section32_3_13 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    val firstCase = success.transform {
      case Success(res) => Success(res * -1)
      case Failure(ex)  => Failure(new Exception("see cause" , ex))
    }
    val secondCase = failure.transform {
      case Success(res) => Success( res * -1 )
      case Failure(ex) => Failure(new Exception("see cause", ex))
    }
    w(1000)
    peep("firstCase = %s, firstCase.value = %s".format(firstCase, firstCase.value))
    peep("secondCause = %s, secondCause.value = %s".format(secondCase, secondCase.value))
  }
  /**
   * Using the overloaded transform, it is possible to transform a failed future into
   * a successful Future
   */
  def section32_3_14 {
    val nonNegative = Future { 1 / 0 } transform {
      case Success(res) => Success(res * -1)
      case Failure(_)   => Success (0)
    }
    w(1000)
    p (nonNegative)
  }
  /**
   * Note
   * Zipping two successful Futures results in a Future of pairs
   */
  def section32_3_15 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    val recovered = failure.recover {
      case ex: Exception => -1
    }
    val zippedSuccess = success zip recovered 
    w(1000)
    p(zippedSuccess)
    
    val zippedFailure = success zip failure 
    w(1000)
    p(zippedFailure)
    
  }
  /**
   * Note: Future.fold has been deprecated as of Scala 2.12 
   */
  def section32_3_16 {
    val fortyTwo = Future { 21 + 21 }
    val fortySix = Future { 23 + 23 } 
    val futureNums = List(fortyTwo , fortySix)
    w(1000)
    p("futureNums" , futureNums)
    val folded = 
      Future.fold(futureNums)(0) { (acc , num) => acc + num}
    w(1000)
    p(folded)
    w(100)
    p("with reduce" , futureNums)
    val reduced  = Future.reduce(futureNums) {
      (acc , num) => acc + num
    }
    w(1000)
    p(reduced)
  }
  
  def section32_3_17 {
       val fortyTwo = Future { 21 + 21 }
    val fortySix = Future { 23 + 23 } 
    val futureNums = List(fortyTwo , fortySix)
    w(1000)
    p("futureNums" , futureNums)
    val futureList = Future.sequence ( futureNums ) 
    w(1000)
    p("futureList", futureList)
    println("traversing futureList")
    val newList = futureList.map{
      n => n map ( _ * 2 )
    }
    w(1000)
    p("newFutureList" , newList)
//    val i = newList.onComplete{
//      list => println("list completed:%s".format(list))
//      _: Exception => println("oops")
//    }
    val i = newList.mapTo[List[Int]]
    w(3000)
    println("i = %ss".format(i))
  }
  /**
   * The Future.traverse method will change a TraversableOnce of any 
   * element type into a TraversableOnce of futures and "sequence" 
   * that into a future TraversableOnce of values. For example, 
   * here a List[Int] is transformed into a Future[List[Int]] 
   * by Future.traverse:
   */
  def section32_3_18 {
    val traversed = Future.traverse( List(1,2,3))  {
      i =>  
        println(i)
        Future {
          val n = i * 2
          println(Thread.currentThread().getName + ":" + n )
          n
        }
    }
    w(1000)
    p("traversed" , traversed)
    val np = traversed flatMap (p => Future(p))
    w(3000)
    println("np = %s".format(np))
    traversed foreach {
      p => println("foreach " + p )
      val np = p.map(_*2)
      println("np = " + np)
    }
  }

  /**
   * Note: foreach is used to perform a side effect only if the Future 
   * succeeds
   */
  def section32_3_19 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    w(1000)
    p("failure", failure)
    p("success" , success)
    failure foreach {
      f => println("f = " + f)
    }
    success foreach {
      s => println("s = " + s)
    }
    
    for ( res <- failure ) println(res)
    for ( res <- success ) println(res)
  }
//// def onComplete[U](f: Try[Int] => U)(implicit executor: ExecutionContext): Unit
//  def callTry[B](f: Try[B] => Unit):Unit = {
//    f {
//      case Success(res) => println(res)
//      case Failure(res) => println(res)
//    }
//  }
  def section32_3_20 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    
    w(1000)
    println("doing success")
    success onComplete {
      case Success(res) => println(res)
      case Failure(ex) => println(ex)
    }
    println("doing failure")
    failure onComplete {
      case Success(res) => println(res)
      case Failure(res) => println(res)
    }
  }
  def section32_3_21 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    w(2000)
    val newFuture = success andThen {
      case Success(res) => println(res)
      case Failure(ex)  => println(ex)
    }
    w(1000)
    p(newFuture)
  }
  def section32_3_22 {
    val nestedFuture = Future { Future { 42 } }
    w(1000)
    p(nestedFuture)
    val flattened = nestedFuture.flatten // Scala 2.12
    w(1000)
    p(flattened)
    
  }
  def section32_3_23 {
    val futNum = Future { 21 + 21 }
    w(1000)
    p("futNum" , futNum)
    val futStr = Future { "ans" + "wer" }
    w(1000)
    p("futStr" , futStr)
    val zipped = futNum zip futStr 
    w(1000)
    p("zipped" , zipped)
    val mapped = zipped map {
      case (num, str) => s"$num is the $str"
    }
    w(1000)
    p("mapped" , mapped)
    
  }
  def section32_3_24 {
    val futNum = Future { 21 + 21 }
    w(1000)
    p("futNum" , futNum)
    val futStr = Future { "ans" + "wer" }
    w(1000)
    p("futStr" , futStr)
    val fut = futNum.zipWith(futStr) {
      case (num,str) => s"$num is the $str"
    }
    w(1000)
    p("fut" , fut)
  }
  def section32_3_25 {
    val failure = Future { 2 / 0 }
    val success = Future { 21 + 21 }
    var flipped =  success.transformWith {
      case Success(res) =>
        Future {
          throw new Exception(res.toString)
        }
      case Failure(ex) => Future{ 21 + 21 }
    }
    w(2000)
    p("flipped" , flipped)
    
      flipped =  failure.transformWith {
      case Success(res) =>
        Future {
          throw new Exception(res.toString)
        }
      case Failure(ex) => Future{ 21 + 21 }
    }
    w(2000)
    p("flipped" , flipped)
  }
  def section32_3_26{
    
    val fut = Future { Thread.sleep(10000); 21 + 21  }
    val x = Await.result(fut, 15.seconds)
    println("x = " + x)
  }
  import org.scalatest.Matchers._
  import org.scalatest.concurrent.ScalaFutures._ 
  def section32_3_27 {
    val fut = Future { Thread.sleep(2000); 21 + 21  }
    val x = Await.result(fut, 15.seconds)
    println("x = " + x)
    x should be ( 42 ) 
  }
  def section32_3_28 {
    implicit val timeout: Duration = 10.second
    val fut = Future { Thread.sleep(2000); 21 + 21 }
    fut.futureValue should be ( 42 ) 
    p("fut" , fut)
    
  }
  section32_3_28
  
}