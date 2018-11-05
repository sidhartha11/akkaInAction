package org.akkainaction.chapter5.main

import org.apache.http.impl.client.CloseableHttpClient
import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import com.google.gson.reflect.TypeToken

import org.akkainaction.chapter5.utilities.Geoutils._
import org.akkainaction.chapter5.entities.ObjectCaseEntities._
import java.lang.reflect.Type

/** needed to execute the Future Call and the Await Call **/
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import java.util.{Date}

/** The Default Execution Context **/
import scala.concurrent.ExecutionContext.Implicits.global

object MainEventService extends App {

  def urlGet = "http://localhost:8080/api/eventrequest"

  def urlCreate = "http://localhost:8080/api/createevent"

  def urlUpdate = "http://localhost:8080/api/updateevent"

  /** callEventService **/
  def callCreateEventService(event: String
      ,location: String
      , timeStamp: String): Long = {

    /**
     * Create One Entity of type EventRequest
     */

    // (implicit tag: ClassTag[B])
    def createOneEntityG[B](eventRequest: => B, token: Type, url: String = "http://localhost:8080/api/createEvent"): Long = {
      emit("testing createOneEntityG:%s".format(eventRequest))

      val spock = eventRequest
      val spockAsJson = new Gson().toJson(eventRequest, token)
      val postingString = new StringEntity(spockAsJson)

      val client: CloseableHttpClient = HttpClients.createDefault();

      emit("creating eventRequest:%s, httpClient:%s".format(spock, client))
      withResources[CloseableHttpClient, Long](client) {
        httpclient =>
          val post = new HttpPost(url)
          // add name value pairs
          post.setEntity(postingString);
          post.setHeader("Content-type", "application/json");
          // send the post request
          val httpResponse: CloseableHttpResponse = client.execute(post)
          val strRes: String = EntityUtils.toString(httpResponse.getEntity)
          val statusCode: Int = httpResponse.getStatusLine.getStatusCode
          println("statusCode = " + statusCode)
          println(strRes)
          strRes.toLong
      }
    }

    /** create one EventRequest **/
    val token: Type = new TypeToken[EventResponse]() {}.getType
    val eventResponse = EventResponse(0, event,location,timeStamp)
    val o = createOneEntityG[EventResponse](eventResponse, token, urlCreate)
    emit("o = %s".format(o))
    o
  }

  def createMultipleEventsToTestWith = {
    for (i <- (1 to 10)) {
      val response: Long = callCreateEventService("Event-" + i + ":id-" + i,"location-" + i , dateToString(new Date))
      emit("created event:%d".format(response))
    }
  }
  
  def callEventService(request: EventRequest): EventResponse = {
    val url = urlGet + "/" 
    emit("callEventService:" + url)
    val eventResponse = getRequestWithResource[EventResponse](request.id,url)
    emit("got " + eventResponse)
    eventResponse
  }
  def doSynchronousCall(ticketNr: Long) = {
    // Listing 5.1. Synchronous call
  val request = EventRequest(ticketNr)
  val response: EventResponse = callEventService(request)
  val event: String = response.event
  emit("event = " + event)

  }
  def doAsynchrounousCall(ticketNr: Long) = {
    // Listing 5.2. Asynchronous call
    val request = EventRequest(ticketNr)
    
    /**
     * Future is created by passing a Block Of Code to the 
     * apply method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request
     */
    val futureEvent: Future[EventResponse] = Future {
      val response = callEventService(request)
      response
    }
    
    /** here we have access to the Future in the calling thread **/
    /** but we need to wait for it to finish **/
    // used by 'time' method
    implicit val baseTime = System.currentTimeMillis
    emit ( "calling Await on the future ")
    val result = Await.result(futureEvent,10 second)
    emit("result=%s".format(result))
    Thread.sleep(1000)
  }
  
  createMultipleEventsToTestWith
  // doSynchronousCall(13)
  // doAsynchrounousCall(15)

}