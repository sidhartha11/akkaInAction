package org.akkainaction.chapter5.utilities
import scala.reflect.ClassTag
import scala.util.control.NonFatal

import org.akkainaction.chapter5.exceptions.CustomExceptions.TrafficServiceException
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import com.google.gson.Gson
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.HttpGet

/** needed to execute the Future Call and the Await Call **/
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

//import java.time.format.DateTimeFormatter


object HttpGenerics {
  val logger = LoggerFactory.getLogger("HttpGenerics")

  def getMethodName =
    new Object() {}
      .getClass()
      .getEnclosingMethod()
      .getName()

  /** generic request call **/
  def genericPostRequestCall[B, R](
    genericRequest: => B,
    url:            String)(implicit tag: ClassTag[B], tag2: ClassTag[R]): R = {

    emit("testing genericPostRequestCall[B,R]:%s".format(genericRequest))

    val postingString = new StringEntity(new Gson().toJson(genericRequest, tag.runtimeClass))
    val client: CloseableHttpClient = HttpClients.createDefault
    emit("creating genericRequest:%s\nurl:%s, httpClient:%s".format(genericRequest, url, client))

    withResources[CloseableHttpClient, R](client) {
      client =>
        val post = new HttpPost(url)
        post.setEntity(postingString)
        post.addHeader("Content-type", "application/json")
        post.addHeader(HttpHeaders.ACCEPT, "application/json")
        val httpResponse: CloseableHttpResponse = client.execute(post)
        val statusCode = httpResponse.getStatusLine.getStatusCode
        emit("statusCode = " + statusCode)
        val e = httpResponse.getEntity
        emit("entity = %s".format(e))
        val jsonString = EntityUtils.toString(e)
        emit("jsonString = %s".format(jsonString))
        val functionReturn =
          statusCode match {
            case 500 => throw new TrafficServiceException("Exception in genericPostRequest:" + statusCode)
            case _ =>
              val response: R = new Gson().fromJson(jsonString, tag2.runtimeClass)
              response
          }
        emit("returning --> ".format(functionReturn))
        functionReturn
    }
  }

  def getRequestWithResource[B](id: Long, url: String = "http://localhost:8080/api/users/")(implicit tag: ClassTag[B]): B = {
    // def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
    emit(">>getRequestWithResource:%d,%s".format(id, url))
    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    withResources[CloseableHttpClient, B](httpClient) { httpclient =>
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
      val r = statusCode match {
        case 500 => throw new TrafficServiceException("Got Service Exception")
        case _ => response
      }
      r
    }
  }

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

  private def closeAndAddSuppressed(
    e:        Throwable,
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

  def emit(message: String, show: Boolean): Unit = {
    if (show) {
      logger.info(message)
    }
  }
  def emit(message: String): Unit = {
    emit(message, true)
  }
  
  def stringToJodaDateTime(date: String): DateTime = {
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    formatter.parseDateTime(date)
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
    def waitForCompletion(future: Future[Any],trace:Boolean): Unit = {
    implicit val baseTime = System.currentTimeMillis
    emit("calling Await on the future ")
    val result = Await.result(future, 10 second)
    emit("result=%s".format(result),trace)
    Thread.sleep(1000)
  }
}