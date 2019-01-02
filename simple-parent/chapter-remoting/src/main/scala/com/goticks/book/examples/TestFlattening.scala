package com.goticks.book.examples

// import scala.concurrent.Future
import scala.concurrent._
// import com.goticks.BoxOffice._
import ExecutionContext.Implicits.global
import java.util.concurrent.CountDownLatch
import scala.util.{ Success, Failure }
import java.util.concurrent.ThreadLocalRandom

/**
 * SCALA-FUTURE simulation illustrating this:
 *
 * Iterable[Future[Option[Event]]] converted to Events[Vector[Event]]
 *
 * This is to illustrate  processing a  collection of Iterables
 * containing Futures of some Option. The simple case classes below
 * are the objects used in this example.
 * Purpose/Use Case:
 * This is taken from a Rest Api example that communicates with a
 * remote Actor. See the book titled Akka In Action, chapter 6 for a full
 * explanation. This is simply an extraction of a restful Get call that
 * sends a message to the remote Actor expecting to retrieve a list of
 * all "Events" maintained by a BoxOffice simulation. How the remote actor works
 * is not the purpose of this illustration. This illustration is just a simple
 * explanation of processing a collecting containing Futures.
 * Keep in mind that in the actual application and this one too , everything is
 * really running asynchronously; though the process is very simple here.
 *
 */

case class Event(name: String, tickets: Int)
case class Events(events: Vector[Event])

object TestFlattening extends App {
  def peep(s: String) = println(Thread.currentThread().getName + " " + s)
  def rwait =
    Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2000))
  def convertToEvents(f: Future[Iterable[Option[Event]]]) = {
    /**
     * The first step will simply flatten things out by removing
     * the Option encapsulation along with any None values found.
     * So We end up with another Future of Iterable[[Event]]
     * The peep statement will probably show None since the thread is
     * not completed.
     */
    val step1 = f.map(p => {
      p.flatten
    })
    peep("step1 = " + step1.value)

    /**
     *  the second step will simply map the Iterable[Event] to the body of
     *  a Events case Object and convert the Iterable to a Vector enclosed in
     *  a Future
     */
    val step2 = step1 map {
      l => Events(l.toVector)
    }
    peep("step2 = " + step2.value)
    // f.map(_.flatten).map( l => Events(l.toVector))
    step2
  }

  /**
   *
   * *******************  MAIN APPLICATION *************************
   *
   * To simulate a Iterable of Futures of Options of some object:
   * Iterable[Future[Option[Event]]
   * so here we have several asynchronous actions taking place in
   * parallel. Each Future is operating in its own thread.
   *
   */
  peep(" creating an Iterable of Futures")
  val ifuts: Iterable[Future[Option[Event]]] = Iterable(
    Future {
      peep("running")
      rwait
      Some(Event("bob", 10))
    }, Future {
      peep("running")
      rwait
      Some(Event("job", 20))
    }, Future {

      peep("running")
      rwait
      None
    }, Future {
      peep("running")
      rwait
      Some(Event("Lob", 30))
    })

  val latch = new CountDownLatch(1)
  peep("processing ifuts = " + ifuts)
  val decouple = Future.sequence(ifuts)
  peep("decoupled ifuts = " + decouple)

  // val rfuts = convertToEvents(Future.sequence(ifuts))
  val rfuts = convertToEvents(decouple)

  rfuts.onComplete {
    case Success(s) =>
      println("we succeeded %s".format(s))
      latch.countDown()
    case Failure(e) =>
      println("oops we failed %s".format(e))
      latch.countDown()
  }

  latch.await()

  rfuts foreach {
    l =>
      {
        println("got l = %s".format(l))
      }
  }

}