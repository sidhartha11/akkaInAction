package org.akkainaction.chapter5.promises

object PromiseObjects  extends App {
  
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import org.akkainaction.chapter5.utilities.Geoutils._
  import scala.util._
  
  def test1 {
  val futureFail = Future {
    throw new Exception("error!")
  }
  
  futureFail.foreach(value => println(value))
  
  waitForCompletion(futureFail)
  }
  
  /** deprecated in 2.12 **/
//  Future
//
//    @deprecated("use `foreach` or `onComplete` instead (keep in mind that they take total rather than partial functions)", "2.12.0")
//    def onSuccess[U](pf: PartialFunction[T, U])(implicit executor: ExecutionContext): Unit
//
//    @deprecated("use `onComplete` or `failed.foreach` instead (keep in mind that they take total rather than partial functions)", "2.12.0")
//    def onFailure[U](@deprecatedName('callback) pf: PartialFunction[Throwable, U])(implicit executor: ExecutionContext): Unit
//
//    @deprecated("use the overloaded version of this method that takes a scala.collection.immutable.Iterable instead", "2.12.0")
//    def find[T](@deprecatedName('futurestravonce) futures: TraversableOnce[Future[T]])(@deprecatedName('predicate) p: T => Boolean)(implicit executor: ExecutionContext): Future[Option[T]] 
//
//    @deprecated("use Future.reduceLeft instead", "2.12.0")
//    def reduce[T, R >: T](futures: TraversableOnce[Future[T]])(op: (R, T) => R)(implicit executor: ExecutionContext): Future[R] = {
  
  def test2 {
    val futureFail = Future {
      //throw new Exception("error!") 
     "<<hello!"
    }
    
    futureFail.onComplete {
      case Success(value) => emit(">>" + value.toString())
      case Failure(value) => emit(">>" + value.toString())
    }
    waitForCompletion(futureFail)
  }
  
  test2
  
}