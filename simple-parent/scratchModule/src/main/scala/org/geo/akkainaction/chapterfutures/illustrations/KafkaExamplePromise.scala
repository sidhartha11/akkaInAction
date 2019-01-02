package org.geo.akkainaction.chapterfutures.illustrations

import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.concurrent.CountDownLatch

import org.geo.akkainaction.chapterfutures.UtilityFunctions._
import scala.util.{ Failure, Success }

case class ProducerRecord(data: String)

case class RecordMetadata(metaData: String)

//object KafkaExamplePromise {
//
//  def sendToKafka(record: ProducerRecord): Future[RecordMetadata] = {
//
//    val promise: Promise[RecordMetadata] = Promise[RecordMetadata]()
//    val future: Future[RecordMetadata] = promise.future
//
//    val callback = new Callback() {
//      def onCompletion(metadata: RecordMetadata, e: Exception): Unit = {
//
//        if ( e != null ) promise.failure(e)
//        else promise.success(metadata)
//
//      }
//    }
//
//    producer.send(record, callback)
//    future
//  }
//}
object runThisThing extends App {

  def nothingGetsPrinted = {
    val latch = new CountDownLatch(1)
    val futureFail = Future {
      waitSim(1000)
      latch.countDown()
      throw new Exception("error!")
    }
    latch.await()
  }

  def somethingGetsPrinted = {
    val latch = new CountDownLatch(1)
    val futureFail = Future {
      waitSim(1000)
      throw new Exception("error!")
    }

    futureFail.onComplete {
      case Success(value) =>
        emitt("value was " + value)
        latch.countDown()
      case Failure(e) =>
        emitt("oops:" + e)
        latch.countDown()
    }
    latch.await()
  }

  val latch = new CountDownLatch(1)
  val futureFail = Future {
    waitSim(1000)
    throw new Exception("error!")
  }

  futureFail.onComplete {
    case Success(value) =>
      emitt("value was " + value)
      latch.countDown()
    case Failure(e) =>
      emitt("oops:" + e)
      latch.countDown()
  }
  latch.await()
}

