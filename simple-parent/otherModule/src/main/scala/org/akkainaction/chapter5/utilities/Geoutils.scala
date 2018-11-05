package org.akkainaction.chapter5.utilities
import java.io._
import java.net._
import scala.collection.mutable.ArrayBuffer
import org.slf4j.LoggerFactory
import scala.util.control.NonFatal
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.util.EntityUtils
import com.google.gson.Gson
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import scala.reflect.ClassTag
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.HttpHeaders
/** needed to execute the Future Call and the Await Call **/
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Geoutils {
  val logger = LoggerFactory.getLogger("Geoutils")

/**
 * withResources:
 * Attempt at creating a Try-with-resources java style implementation
 * r: => T   a thunk, call by name object that is not activated until used the first time
 * f: T => V  a functional argument that takes a resource, T, and returns a V after operating on it
 * Returns an object of type V, the result of the operation.
 * 
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

  /**
   * An attempt at a generic function that executes an HTTP get request
   * and returns an object of the generic type. The HTTP get request 
   * returns a mime type of application/json, which is hard-coded in the function
   */
  @throws(classOf[Exception])
  def getRequest[B](id: Long, url: String = "http://localhost:8080/api/users/")(implicit tag: ClassTag[B]): B = {

    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    try {
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val content: String = EntityUtils.toString(httpResponse.getEntity)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val users: B = new Gson().fromJson(content, tag.runtimeClass)
      emit("statusCode = " + statusCode)
      emit("converted Users=" + users)
      users
    } catch {
      case e: IOException => {
        emit("<<<<Exception caught:" + e)
        throw e
      }
    } finally {
      emit("getRequest finally block called")
      httpClient.close()
    }
  }
    /**
   * Example using withResources 
   */
  @throws(classOf[Exception])
  def getRequestWithResource[B](id: Long, url: String = "http://localhost:8080/api/users/")(implicit tag: ClassTag[B]): B = {
  // def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
    emit(">>getRequestWithResource:%d,%s".format(id,url))
    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    withResources[CloseableHttpClient,B](httpClient){httpclient => 
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val e = httpResponse.getEntity
      emit("entity=%s".format(e))
      val content: String = EntityUtils.toString(e)
      emit("content=" + content)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val response: B = new Gson().fromJson(content, tag.runtimeClass)
      emit("statusCode = " + statusCode)
      emit("converted Response=" + response)
      response
    }
  }
  /**
   * Example using withResources 
   */
  @throws(classOf[Exception])
  def getRequestUsersRsource[B](id: Long, url: String = "http://localhost:8080/api/users/")(implicit tag: ClassTag[B]): B = {
  // def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {

    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    withResources[CloseableHttpClient,B](httpClient){httpclient => 
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val content: String = EntityUtils.toString(httpResponse.getEntity)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val users: B = new Gson().fromJson(content, tag.runtimeClass)
      emit("statusCode = " + statusCode)
      emit("converted Users=" + users)
      users
    }
  }
  
    /**
   * Example using withResources 
   */
  @throws(classOf[Exception])
  def postRequestUsersRsource[B](id: Long, url: String = "http://localhost:8080/api/users/")(implicit tag: ClassTag[B]): B = {
  // def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {

    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    withResources[CloseableHttpClient,B](httpClient){httpclient => 
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val content: String = EntityUtils.toString(httpResponse.getEntity)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val users: B = new Gson().fromJson(content, tag.runtimeClass)
      emit("statusCode = " + statusCode)
      emit("converted Users=" + users)
      users
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
  
  
  def getFile(uri: String): File = {
    new File(uri)
  }

  def emit(message: String, show: Boolean): Unit = {
    if (show) {
      logger.info(message)
    }
  }
  def emit(message: String): Unit = {
    emit(message, true)
  }
  
  def dateToString(date: java.util.Date): String = {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    format.format(date)
  }
  
  def waitForCompletion(future: Future[Any]): Unit = {
    implicit val baseTime = System.currentTimeMillis
    emit("calling Await on the future ")
    val result = Await.result(future, 10 second)
    emit("result=%s".format(result))
    Thread.sleep(1000)
    
  }
}