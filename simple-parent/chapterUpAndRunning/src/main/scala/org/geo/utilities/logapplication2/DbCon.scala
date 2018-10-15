package org.geo.utilities.logapplication2


import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.Observable
import com.typesafe.sslconfig.util.LoggerFactory
import org.slf4j.LoggerFactory
import org.mongodb.scala.{Completed,Observer,Observable}

class DbCon(url: String) {
  
  import org.geo.utilities.Geoutils._
  
  /*
   * Writes a map to a database.
   * @param map the map to write to the database.
   * @throws DbBrokenonnectionException when the connection is broken. 
   * it might be broken later
   * @throws DbNodeDownException when the database Node has been removed 
   * from the database cluster
   */
  
  val logger = LoggerFactory.getLogger(classOf[DbCon])
  logger.debug(myName + " Constructor")
  
  /** set up database connection **/
val client:MongoClient = MongoClient(url)
// val client:MongoClient = MongoClient("mongodb://localhost:27017")

val database = client.getDatabase("test")
val collection:MongoCollection[Document] = database.getCollection("akkaTest")
  
  
  def write(map: Map[Symbol, Any]): Unit = {
    emit(myName + " writing record",true)
    emit("%s map=%s".format(myName,map),true)
    
    val document: Document = Document (
        "time" -> map.get('time).get.toString ,
        "message" -> map.get('message).get.toString ,
        "messageType" -> map.get('messageType).get.toString
        )
        
//    logger.debug(myName + " in write")
//    logger.debug("%s map=%s".format(myName,map))
    
    val insertObservable: Observable[Completed] = collection.insertOne(document)
    
    emit(myName + " inserting into database",true)
    
    insertObservable.subscribe(new Observer[Completed] {
      
      override def onNext(result: Completed): Unit = logger.debug(s"onNext: $result")
      override def onError(e: Throwable): Unit =logger.debug(s"onError: $e")
      override def onComplete(): Unit = logger.debug("onComplete")
    })

  }
  def close(): Unit = {
     emit(myName + " in close",true)
     client.close()
  }
  def myName = "DbCon"
}