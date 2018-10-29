package org.akkainaction.chapter5.main

import org.akkainaction.chapter5.utilities.Geoutils._
import org.akkainaction.chapter5.entities.{ Gusers, Users }
import org.apache.http.impl.client.CloseableHttpClient
import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.util.EntityUtils
// import org.scalactic.Or.B
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.CloseableHttpResponse
import scala.reflect.ClassTag
import com.google.gson.reflect.TypeToken


object Main extends App {

  def url = "http://localhost:8080/api/users"
  /** get one Entity **/
  def getAnEntity(id: Long) = {
    val users = getRequestUsersRsource[Gusers](id)
    emit("got " + users)
  }

  def createAListOfEntities(ids: List[Long]): List[Long] = {
    val b = for (i <- ids) yield {
      i
    }
    b.toList
  }
  
  /**
   * using a range of ids, create a list of  items in the data base
   * by sending range number of ids to be created 
   */
  def createAListOfEntitiesr(ids: Range, url: String="http://localhost:8080/api/update"): List[Long] = {
    emit("testing createAListOfEntitiesr:" + ids)
    val b = for (i <- ids) yield {
      val spock = new Users(i, "user-" + i, "pw-" + i, true)
      val spockAsJson = new Gson().toJson(spock)
      val postingString = new StringEntity(spockAsJson)

      val client: CloseableHttpClient = HttpClients.createDefault();

      emit("creating user:%s, httpClient:%s".format(spock,client))
      withResources[CloseableHttpClient, Long](client) {
        httpclient =>
          val post = new HttpPost(url)
          // add name value pairs
          post.setEntity(postingString);
          post.setHeader("Content-type", "application/json");
          // send the post request
          val httpResponse: CloseableHttpResponse = client.execute(post)
          val strRes : String = EntityUtils.toString(httpResponse.getEntity)
          val statusCode: Int = httpResponse.getStatusLine.getStatusCode
          println("statusCode = " + statusCode)
          println(strRes)
          strRes.toLong
      }
    }
    b.toList
  }
    /**
     * Create One Entity of type User
     */
    def createOneEntity(user:String, password:String, url: String="http://localhost:8080/api/users"): Long = {
    emit("testing createOneEntity:%s,%s,%s".format(user,password,url))
    
      val spock = new Users(0,user,password, true)
      val spockAsJson = new Gson().toJson(spock)
      val postingString = new StringEntity(spockAsJson)

      val client: CloseableHttpClient = HttpClients.createDefault();

      emit("creating user:%s, httpClient:%s".format(spock,client))
      withResources[CloseableHttpClient, Long](client) {
        httpclient =>
          val post = new HttpPost(url)
          // add name value pairs
          post.setEntity(postingString);
          post.setHeader("Content-type", "application/json");
          // send the post request
          val httpResponse: CloseableHttpResponse = client.execute(post)
          val strRes : String = EntityUtils.toString(httpResponse.getEntity)
          val statusCode: Int = httpResponse.getStatusLine.getStatusCode
          println("statusCode = " + statusCode)
          println(strRes)
          strRes.toLong
      }
  }
    
        /**
     * Create One Entity of type User
     */
    import java.lang.reflect.Type
    // (implicit tag: ClassTag[B])
    def createOneEntityG[B](user: => B, token: Type, url: String="http://localhost:8080/api/users"): Long = {
    emit("testing createOneEntityG:%s".format(user))
    
      val spock = user
      val spockAsJson = new Gson().toJson(user,token)
      val postingString = new StringEntity(spockAsJson)

      val client: CloseableHttpClient = HttpClients.createDefault();

      emit("creating user:%s, httpClient:%s".format(spock,client))
      withResources[CloseableHttpClient, Long](client) {
        httpclient =>
          val post = new HttpPost(url)
          // add name value pairs
          post.setEntity(postingString);
          post.setHeader("Content-type", "application/json");
          // send the post request
          val httpResponse: CloseableHttpResponse = client.execute(post)
          val strRes : String = EntityUtils.toString(httpResponse.getEntity)
          val statusCode: Int = httpResponse.getStatusLine.getStatusCode
          println("statusCode = " + statusCode)
          println(strRes)
          strRes.toLong
      }
  }

    def exampleGenericToGson = {
    val token: Type = new TypeToken[Users](){}.getType
    val users: Users = Users(0,"Bob2","bob",true)
    val o = createOneEntityG[Users](users,token, url)
    emit("o = %s".format(o))
    }
    
    exampleGenericToGson
}