package com.georgecurington.pis.chapter32.futures
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import java.util.concurrent.CountDownLatch
import scala.concurrent.{Future}

object Utils {
  var latch: CountDownLatch = _
  def glock(num: Int) = {
    latch = new CountDownLatch(num)
  }

  def hlock() = {
    latch.await()
  }
  def rlock() = {
    latch.countDown()
  }

  def stop = {
    ec.threadPool.shutdownNow()
  }
  implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(20);

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }

  def peep(fut: Future[_]) {
    println(fut)
  }
  def p( fut: Future[_]) {
    println("fut = %s , fut.value = %s".format(fut , fut.value))
  }
  def p( msg: String , fut: Future[_]) {
    println("%s:fut = %s , fut.value = %s".format(msg,fut , fut.value))
  }
   def p2( msg:String , fut: Future[List[_]]) {
    println("%s: fut = %s , fut.value = %s".format(msg, fut, fut.value ))
  }
  def p( msg:String , fut: List[Future[_]]) {
    println("%s: fut = %s ".format(msg, fut ))
  }
    def peep(fut: String) {
    println(fut)
  }
    
  def w(time: Long) {
    Thread.sleep(time)
  }
  
  def hang {
    glock(1)
    hlock
  }
}