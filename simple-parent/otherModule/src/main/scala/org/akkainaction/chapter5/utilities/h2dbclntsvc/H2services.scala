package org.akkainaction.chapter5.utilities.h2dbclntsvc

import scala.util.control.NonFatal
import org.akkainaction.chapter5.utilities.Geoutils._
object H2services {
  
  /**
   * A method of trying to emulate the java Closeable
   * Try block
   * Here we have withResources that takes a generic parameter that 
   * is a subset of the AutoCloseable interface. This function takes
   * a call by name function that returns a T,
   * a function that takes a T and returns a V.
   * The return value from the function is a V. 
   * The call by name function will not be activated until explicitly used
   * the first time. The resource is the call by name parameter.
   * The functional argument will operate upon the resource. 
   */
  def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
  val resource: T = r
  require(resource != null, "resource is null")
  var exception: Throwable = null
  try {
    emit("calling resource block")
    f(resource)
  } catch {
    case NonFatal(e) =>
      exception = e
      throw e
  } finally {
    emit("calling closeAndAddSuppressed")
    closeAndAddSuppressed(exception, resource)
  }
}

private def closeAndAddSuppressed(e: Throwable,
                                  resource: AutoCloseable): Unit = {
  if (e != null) {
    try {
      emit("1 closing resource")
      resource.close()
    } catch {
      case NonFatal(suppressed) =>
        e.addSuppressed(suppressed)
    }
  } else {
    emit("2 closing resource")
    resource.close()
  }
}
}