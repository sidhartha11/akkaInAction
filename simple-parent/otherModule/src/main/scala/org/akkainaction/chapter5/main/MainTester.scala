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
import java.lang.reflect.Type

/** needed to execute the Future Call and the Await Call **/
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import java.util.{ Date }

/** The Default Execution Context **/
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag
import com.google.gson.reflect.TypeToken

import org.akkainaction.chapter5.entities.ObjectCaseEntities.{
  TrafficRequest,
  TrafficResponse,
  EventResponse,
  EventRequest
}
object MainTester {
  def urlGet = "http://localhost:8080/api/eventrequest"
  def urlPostRoute = "http://localhost:8080/api/trafficrequest"

  /** callEventService **/
  /**
   * This function takes a lazy evaluated generic Request object that will be
   * posted to a Restful WebService.
   * @param genericRequest:B lazy evaluated case class representing a json object to post to a service
   * @param token:Type The type of the case class being posted(java.lang.reflect.Type)
   * @param url:String The url endpoint where the rest Service lives
   */

  def callCreateGenericService[B](genericRequest: => B, token: Type, url: String = "http://localhost:8080/api/unknown"): Long = {

    /**
     * This method is actually a dup of the outer call and is not strictly required.
     * However, you may do some post and pre processing here if needed.
     */

    def createOneEntityGO[B, R](genericRequest: => B, token: Type, url: String = "http://localhost:8080/api/createEvent")(implicit tag: ClassTag[R]): R = {

      emit("testing createOneEntityGO[B,R]:%s".format(genericRequest))

      /**
       * new StringEntity( new Gson().toJson(GenericRequest,token))
       * This converts the generic object to a Json String:
       * jsonString = new Gson().toJson(GenericRequest, token)
       * The json string is then converted to an StringEntity that is usable the the http client
       * new StringEntity(jsonString)
       */
      val postingString = new StringEntity(new Gson().toJson(genericRequest, token))
      /** create a http client to process the post **/
      val client: CloseableHttpClient = HttpClients.createDefault
      emit("creating genericRequest:%s,\nurl:%s, httpClient:%s".format(genericRequest, url, client))
      /** use the withResources trait to process the post request and clean up resources **/
      withResources[CloseableHttpClient, R](client) {
        httpClient =>
          val post = new HttpPost(url)
          post.setEntity(postingString)
          post.setHeader("Content-type", "application/json")
          /** send the post request **/
          val httpResponse: CloseableHttpResponse = client.execute(post)
          /** convert the response to a json String **/
          val strRes: String = EntityUtils.toString(httpResponse.getEntity)
          /** convert the json String to the actual type of object we expect */
          val response: R = new Gson().fromJson(strRes, tag.runtimeClass)
          /** display the status code we got back from the rest service **/
          emit("statusCode = " + httpResponse.getStatusLine.getStatusCode)
          /** display the json string **/
          emit("json string=" + strRes)
          response
      }
    }
    /** create one EventRequest **/
    val o = createOneEntityGO[B, Long](genericRequest, token, url)
    /** display the converted object **/
    o
  }

  /**
   * This will insert multiple TrafficRequest rows in base by sending
   * multiple requests to the endpoint Rest Service
   */

  def createMultipleTraficToTestWith {
    val token: Type = new TypeToken[TrafficRequest]() {}.getType
    for (i <- (1 to 10)) {
      val request = TrafficRequest(0, "distination-" + i, dateToString(new java.util.Date))
      val response: Long = callCreateGenericService[TrafficRequest](request, token, "http://localhost:8080/api/createtrafficrequest")
      emit("created trafficRequest:%d".format(response))
    }
  }

  /**
   * This will insert multiple EventResponse rows in database by sending
   * multiple requests to the endpoint Rest Service
   */

  def createMultipleEventsToTestWith = {
    /** create a Type object representing the request type we are sending **/
    val token: Type = new TypeToken[EventResponse]() {}.getType
    for (i <- (1 to 10)) {
      val request = EventResponse(0, "event-" + i, "location-" + i, dateToString(new java.util.Date))
      val response: Long = callCreateGenericService[EventResponse](request, token, "http://localhost:8080/api/createevent")
      emit("created EventResponse:%d".format(response))
    }
  }

  def callEventService(request: EventRequest): EventResponse = {
    val url = urlGet + "/"
    emit("callEventService:" + url)
    val eventResponse = getRequestWithResource[EventResponse](request.id, url)
    emit("got " + eventResponse)
    eventResponse
  }

  def doAsynchrounousCall52(ticketNr: Long) = {
    // Listing 5.2 Asynchronous call
    val request = EventRequest(ticketNr)

    /**
     * Future is created by passing a Block of Code to the apply
     * method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request.
     */
    val futureEvent: Future[EventResponse] = Future {
      val response = callEventService(request)
      response
    }
    /** now we need to wait a bit **/

    implicit val baseTime = System.currentTimeMillis
    emit("calling Await on the future ")
    val result = Await.result(futureEvent, 10 second)
    emit("result=%s".format(result))
    Thread.sleep(1000)
  }
  /**
   * rework of generic service
   * This time I am trying to have both the request and response be passed in as 
   * generic type parameters. This, however, does not work because of the Type token that is
   * being passed in. The Type token only applies to the request object. 
   * See below for a third attempt at doing this in which I pass in 2 type parameters, one
   * for the response and one for the request.
   * NOTE: I am sure there is a better way ... but I am just now learning scala!!!
   */
  def callCreateGenericServiceG[B, R](genericRequest: => B, token: Type, url: String = "http://localhost:8080/api/unknown")(implicit tag: ClassTag[B]): TrafficResponse = {

    /**
     * This method is actually a dup of the outer call and is not strictly required.
     * However, you may do some post and pre processing here if needed.
     */

    def createOneEntityGO[B](genericRequest: => B, token: Type, url: String = "http://localhost:8080/api/createEvent"): TrafficResponse = {

      emit("testing createOneEntityGO[B,R]:%s".format(genericRequest))

      /**
       * new StringEntity( new Gson().toJson(GenericRequest,token))
       * This converts the generic object to a Json String:
       * jsonString = new Gson().toJson(GenericRequest, token)
       * The json string is then converted to an StringEntity that is usable the the http client
       * new StringEntity(jsonString)
       */
      val postingString = new StringEntity(new Gson().toJson(genericRequest, token))
      /** create a http client to process the post **/
      val client: CloseableHttpClient = HttpClients.createDefault
      emit("creating genericRequest:%s,\nurl:%s, httpClient:%s".format(genericRequest, url, client))
      /** use the withResources trait to process the post request and clean up resources **/
      withResources[CloseableHttpClient, TrafficResponse](client) {
        httpClient =>
          val post = new HttpPost(url)
          post.setEntity(postingString)
          post.setHeader("Content-type", "application/json")
          /** send the post request **/
          val httpResponse: CloseableHttpResponse = client.execute(post)
          /** convert the response to a json String **/
          val strRes: String = EntityUtils.toString(httpResponse.getEntity)
          /** convert the json String to the actual type of object we expect */
          val response: TrafficResponse = new Gson().fromJson(strRes, classOf[TrafficResponse])
          /** display the status code we got back from the rest service **/
          emit("statusCode = " + httpResponse.getStatusLine.getStatusCode)
          /** display the json string **/
          emit("json string=" + strRes)
          response
      }
    }
    /** create one EventRequest **/
    val o = createOneEntityGO[B](genericRequest, token, url)
    /** display the converted object **/
    o
  }

  /**
   * rework of generic apache http client call in scala
   * This time, I discovered that it is possible to apply multiple implicit parameters
   * to the function declaration. This seems to primitively fix the problem of having 
   * both response and request parameters as generic type Parameters. Of course,
   * explicit Type information must be supplied for the underlying Gson calls that require
   * a "Type". For now this will due until I learn more. This exercise is only being done in
   * support of the Book "AkkaInAction" in order to create a Spring Boot application as the 
   * container for the Restful Service Calls that the book requires in order to complete
   * the exercises covering Scala Future Trait. 
   * Here is the declaration. 
   */
  def callCreateGenericServiceG2[B, R]
          (genericRequest: => B, 
              token: Type, 
              token2: Type, 
              url: String = "http://localhost:8080/api/unknown")
              (implicit tag: ClassTag[B], tag2: ClassTag[R]): R = {

 

    def createOneEntityGO[B, R](genericRequest: => B, token: Type, token2: Type, url: String = "http://localhost:8080/api/createEvent"): R = {

      emit("testing createOneEntityGO[B,R]:%s".format(genericRequest))

      /**
       * new StringEntity( new Gson().toJson(GenericRequest,token))
       * This converts the generic object to a Json String:
       * jsonString = new Gson().toJson(GenericRequest, token)
       * The json string is then converted to an StringEntity that is usable the the http client
       * new StringEntity(jsonString)
       */
      val postingString = new StringEntity(new Gson().toJson(genericRequest, token))
      /** create a http client to process the post **/
      val client: CloseableHttpClient = HttpClients.createDefault
      emit("creating genericRequest:%s,\nurl:%s, httpClient:%s".format(genericRequest, url, client))
      /** use the withResources trait to process the post request and clean up resources **/
      withResources[CloseableHttpClient, R](client) {
        httpClient =>
          val post = new HttpPost(url)
          post.setEntity(postingString)
          post.setHeader("Content-type", "application/json")
          /** send the post request **/
          val httpResponse: CloseableHttpResponse = client.execute(post)
          /** convert the response to a json String **/
          val strRes: String = EntityUtils.toString(httpResponse.getEntity)
          /** convert the json String to the actual type of object we expect */
          val response: R = new Gson().fromJson(strRes, token2)
          /** display the status code we got back from the rest service **/
          emit("statusCode = " + httpResponse.getStatusLine.getStatusCode)
          /** display the json string **/
          emit("json string=" + strRes)
          response
      }
    }
    /** create one EventRequest **/
    val o = createOneEntityGO[B, R](genericRequest, token, token2, url)
    /** display the converted object **/
    o
  }

  /**
   * This call illustrates a few uses of the Future trait in scala
   * Book AkkaInAction exercise 5.3
   */
  def doAsynchrounousCall53(ticketNr: Long) = {

    // Listing 5.3 Asynchronous call
    /** first create a EventRequest **/
    val request = EventRequest(ticketNr)

    /**
     * Future is created by passing a Block of Code to the apply
     * method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request.
     */
    /** runs in a separate Thread **/
    val futureEvent: Future[EventResponse] = Future {
      val response = callEventService(request)
      response
    }

    /** process the Future Option using foreach **/
    /**
     * The code block below is only called when the callEventService above is successfull
     * and completed.
     */
    emit("calling foreach on future")
    futureEvent.foreach { event =>
      val trafficRequest = TrafficRequest(
        id = 0,
        destination = event.location,
        arrivalTime = event.timeStamp)
      /** now call the generic service with a proper class Tag **/
      val token: Type = new TypeToken[TrafficRequest]() {}.getType
      val token2: Type = new TypeToken[TrafficResponse]() {}.getType

      //      val trafficResponse = callCreateGenericServiceG[TrafficRequest, TrafficResponse](trafficRequest, token,urlPostRoute)
      val trafficResponse = callCreateGenericServiceG2[TrafficRequest, TrafficResponse](trafficRequest, token, token2, urlPostRoute)
      emit(trafficResponse.route)
    }

    /** now we need to wait a bit **/

       waitForCompletion(futureEvent)

  }
  
    /**
   * This call illustrates a few uses of the Future trait in scala
   * Book AkkaInAction exercise 5.4
   */
  def doAsynchrounousCall54(ticketNr: Long) = {

    // Listing 5.3 Asynchronous call
    /** first create a EventRequest **/
    val request = EventRequest(ticketNr)

    /**
     * Future is created by passing a Block of Code to the apply
     * method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request.
     */
    /** runs in a separate Thread **/
    val futureEvent: Future[EventResponse] = Future {
      val response = callEventService(request)
      response
    }

    /** process the Future Option using foreach **/
    /**
     * The code block below is only called when the callEventService above is successfull
     * and completed.
     */
    emit("calling map on future")
    val futureRoute: Future[String] = futureEvent.map { event =>
      val trafficRequest = TrafficRequest(
        id = 0,
        destination = event.location,
        arrivalTime = event.timeStamp
        )
      /** now call the generic service with a proper class Tag **/
      val token: Type = new TypeToken[TrafficRequest]() {}.getType
      val token2: Type = new TypeToken[TrafficResponse]() {}.getType

      /**
       * Synchronous call here still , but the trafficResponse is wrapped
       * in a Future and returned.
       */
      val trafficResponse = callCreateGenericServiceG2[TrafficRequest, TrafficResponse](trafficRequest, token, token2, urlPostRoute)
      emit("trafficResponse.route=%s".format(trafficResponse.route))
      trafficResponse.route
    }

    emit("futureRoute = %s".format(futureRoute.foreach(x => x)))
    /** now we need to wait a bit **/

   waitForCompletion(futureRoute)
  }
  
      /**
   * This call illustrates a few uses of the Future trait in scala
   * Book AkkaInAction exercise 5.5
   */
  def doAsynchrounousCall55(ticketNr: Long) = {

    // Listing 5.5 Asynchronous call
    /** first create a EventRequest **/
    val request = EventRequest(ticketNr)

    /**
     * Future is created by passing a Block of Code to the apply
     * method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request.
     */
    /**
     * Now chain the getRoute method with Future[Route] result
     */
    
    val futureRoute1: Future[String] = Future {
      callEventService(request)
    }.map{ event => 
      val trafficRequest = TrafficRequest (
          id = 0,
          destination = event.location ,
          arrivalTime = event.timeStamp
          )
      /** now call the generic service with a proper class Tag **/
      val token: Type = new TypeToken[TrafficRequest]() {}.getType
      val token2: Type = new TypeToken[TrafficResponse]() {}.getType
      callCreateGenericServiceG2[TrafficRequest, TrafficResponse](trafficRequest, token, token2, urlPostRoute).route 
    }

    emit("futureRoute1 = %s".format(futureRoute1.foreach(x => x)))
    /** now we need to wait a bit **/

   waitForCompletion(futureRoute1)
  }

        /**
   * This call illustrates a few uses of the Future trait in scala
   * Book AkkaInAction exercise 5.6
   */
  def getEvent(ticketNr: Long): Future[EventResponse] = {
        /** first create a EventRequest **/
    val request = EventRequest(ticketNr)
    Future {
      callEventService(request)
    }
  }
  
  def getRoute(event: EventResponse):Future[String] = {
         val trafficRequest = TrafficRequest (
          id = 0,
          destination = event.location ,
          arrivalTime = event.timeStamp
          )
      /** now call the generic service with a proper class Tag **/
      val token: Type = new TypeToken[TrafficRequest]() {}.getType
      val token2: Type = new TypeToken[TrafficResponse]() {}.getType
      Future {
      callCreateGenericServiceG2[TrafficRequest, TrafficResponse](trafficRequest, token, token2, urlPostRoute).route 
      }
  }
  def doAsynchrounousCall55b (ticketNr: Long) = {

    // Listing 5.6 Asynchronous call
    /** first create a EventRequest **/
    val request = EventRequest(ticketNr)

    /**
     * Future is created by passing a Block of Code to the apply
     * method of the Future Object
     * Note that we are closing over a value from the outer thread,
     * request.
     */
    /**
     * Now chain the getRoute method with Future[Route] result
     */
    
    val futureRoute1: Future[String] = Future {
      callEventService(request)
    }.map{ event => 
      val trafficRequest = TrafficRequest (
          id = 0,
          destination = event.location ,
          arrivalTime = event.timeStamp
          )
      /** now call the generic service with a proper class Tag **/
      val token: Type = new TypeToken[TrafficRequest]() {}.getType
      val token2: Type = new TypeToken[TrafficResponse]() {}.getType
      callCreateGenericServiceG2[TrafficRequest, TrafficResponse](trafficRequest, token, token2, urlPostRoute).route 
    }

    emit("futureRoute1 = %s".format(futureRoute1.foreach(x => x)))
    /** now we need to wait a bit **/

   waitForCompletion(futureRoute1)
  }
  
  def populateTables = {
    createMultipleTraficToTestWith
    createMultipleEventsToTestWith
  }
  
  def doAsynchronousCall56(ticketNr: Long): Future[String] = {
    val futureRoute: Future[String] = 
      getEvent(ticketNr).flatMap { 
      event => getRoute(event)
    }
    futureRoute
  }

  def main(args: Array[String]) {
    // doAsynchrounousCall52(10)
    val f: Future[String] = doAsynchronousCall56(8) 
    waitForCompletion(f)
  }
}