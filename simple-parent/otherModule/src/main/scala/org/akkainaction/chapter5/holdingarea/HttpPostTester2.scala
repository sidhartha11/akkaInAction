package org.akkainaction.chapter5.holdingarea

import java.util.ArrayList

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import java.io.IOException
import scala.reflect.ClassTag
import scala.util.control.NonFatal

case class Person(firstName: String, lastName: String, age: Int)

case class Users(id: Long, username: String, password: String, enabled: Boolean)
trait RequestUser {
  
}
class Gusers extends RequestUser {
  var id: Long = _
  var username: String = _ 
  var password: String = _
  var enabled: Boolean = _ 
  override def toString = s"Gusers:[%d,%s, %s, %b]".format(id,username,password,enabled)
}

object HttpPostTester2 {
  
def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
  val resource: T = r
  require(resource != null, "resource is null")
  var exception: Throwable = null
  try {
    println("calling resource block")
    f(resource)
  } catch {
    case NonFatal(e) =>
      exception = e
      throw e
  } finally {
    println("calling closeAndAddSuppressed")
    closeAndAddSuppressed(exception, resource)
  }
}

private def closeAndAddSuppressed(e: Throwable,
                                  resource: AutoCloseable): Unit = {
  if (e != null) {
    try {
      println("1 closing resource")
      resource.close()
    } catch {
      case NonFatal(suppressed) =>
        e.addSuppressed(suppressed)
    }
  } else {
    println("2 closing resource")
    resource.close()
  }
}
  
  @throws(classOf[Exception])
  def getRequestUsersRsource(id: Long, url: String = "http://localhost:8080/api/users/"): Users = {
  // def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {

    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    withResources[CloseableHttpClient,Users](httpClient){httpclient => 
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val content: String = EntityUtils.toString(httpResponse.getEntity)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val users: Users = new Gson().fromJson(content, classOf[Users])
      println("statusCode = " + statusCode)
      println("converted Users=" + users)
      users
    }
  }

  @throws(classOf[Exception])
  def getRequestUsers(id: Long, url: String = "http://localhost:8080/api/users/"): Users = {

    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()

    try {
      val getRequest: HttpUriRequest = new HttpGet(url + id)
      getRequest.addHeader(HttpHeaders.ACCEPT, "application/json")
      val httpResponse: CloseableHttpResponse = httpClient.execute(getRequest)
      val content: String = EntityUtils.toString(httpResponse.getEntity)
      val statusCode: Int = httpResponse.getStatusLine.getStatusCode
      val users: Users = new Gson().fromJson(content, classOf[Users])
      println("statusCode = " + statusCode)
      println("converted Users=" + users)
      users
    } catch {
      case e: IOException => {
        println("<<<<Exception caught:" + e)
        throw e
      }
    } finally {
      println("finally called")
      httpClient.close()
    }
  }
  
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
      println("statusCode = " + statusCode)
      println("converted Users=" + users)
      users
    } catch {
      case e: IOException => {
        println("<<<<Exception caught:" + e)
        throw e
      }
    } finally {
      httpClient.close()
    }
  }

  def postUrlEncodedFormEntity = {
    // create our object as a json string
    val spock = new Users(0, "Leonard", "Nimoy", true)
    val spockAsJson = new Gson().toJson(spock)

    val url = "http://localhost:8080/api/users";
    //val client = new DefaultHttpClient

    val client: CloseableHttpClient = HttpClients.createDefault();

    val post = new HttpPost(url)

    // add name value pairs
    val nameValuePairs = new ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", spockAsJson))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    // send the post request
    val response = client.execute(post)
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
  }
  def postJsonEntity(id: Long, first: String, last: String) = {
    // create our object as a json string
    val spock = new Users(id, first, last, true)
    val spockAsJson = new Gson().toJson(spock)
    val postingString = new StringEntity(spockAsJson)

    val url = "http://localhost:8080/api/users"
    //val client = new DefaultHttpClient

    val client: CloseableHttpClient = HttpClients.createDefault();

    val post = new HttpPost(url)

    // add name value pairs
    post.setEntity(postingString);
    post.setHeader("Content-type", "application/json");

    // send the post request
    val response = client.execute(post)
    // println("--- HEADERS ---")
    // response.getAllHeaders.foreach(arg => println(arg))
    val strRes = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    println(strRes)
    strRes.toLong
  }

  def updateJsonEntity(id: Long, first: String, last: String) = {
    // create our object as a json string
    val spock = new Users(id, first, last, true)
    val spockAsJson = new Gson().toJson(spock)
    val postingString = new StringEntity(spockAsJson)

    val url = "http://localhost:8080/api/update"
    //val client = new DefaultHttpClient

    val client: CloseableHttpClient = HttpClients.createDefault();

    val post = new HttpPost(url)

    // add name value pairs
    post.setEntity(postingString);
    post.setHeader("Content-type", "application/json");

    // send the post request
    val response = client.execute(post)
    // println("--- HEADERS ---")
    // response.getAllHeaders.foreach(arg => println(arg))
    val strRes = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    println(strRes)
    strRes.toLong
  }

  def deleteJsonEntity(name: String, password: String) = {
    // create our object as a json string
    println("enerd deleteJsonEntity")
    val spock = new Users(10, name, password, true)
    val spockAsJson = new Gson().toJson(spock)
    val postingString = new StringEntity(spockAsJson)

    val url = "http://localhost:8080/api/delete"
    //val client = new DefaultHttpClient

    val client: CloseableHttpClient = HttpClients.createDefault();

    val post = new HttpPost(url)

    // add name value pairs
    post.setEntity(postingString);
    post.setHeader("Content-type", "application/json");

    // send the post request
    val response = client.execute(post)
    // println("--- HEADERS ---")
    // response.getAllHeaders.foreach(arg => println(arg))
    val strRes = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    println(strRes)
  }

  def getJsonEntity(id: Long) = {
    // create our object as a json string
    println("enterd getJsonEntity")
    val url = "http://localhost:8080/api/users/" + id
    //val client = new DefaultHttpClient
    val client: CloseableHttpClient = HttpClients.createDefault();
    val get = new HttpGet(url)

    get.setHeader("accept", "application/json");

    // send the post request
    val response = client.execute(get)
    // println("--- HEADERS ---")
    // response.getAllHeaders.foreach(arg => println(arg))
    val strRes = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    println(strRes)
  }

  def testGetEntries = {
    for (i <- 3 to 12) {
      val id = updateJsonEntity(i, "George-" + i, "george-" + (i * 3))
      // deleteJsonEntity("GeorgeX","")
      println("id = " + id)
      getJsonEntity(id)
    }
  }

  def getAEntity = {
    val users = getRequestUsersRsource(10)
    println("got " + users)
  }
  def main(args: Array[String]) {
    //val users = getRequest[Gusers](10)
    val users = getRequestUsersRsource(10)
    println("got " + users)
  }

}