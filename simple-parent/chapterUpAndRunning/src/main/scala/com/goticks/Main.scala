package com.goticks

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout

import com.typesafe.config.{ Config, ConfigFactory }

object Main extends App
    with RequestTimeout {
  println("running ")
  
  val config = ConfigFactory.load() 
  val host = config.getString("http.host") // Gets the host and a port from the configuration
  println("host = %s".format(host))
  val port = config.getInt("http.port")
  println("port = %s".format(port))

  /** 
   *  creates the RestApi, 
   *  gets the HTTP extension, 
   *  and binds the RestApi routes to the HTTP extension.
   */
  implicit val system = ActorSystem()  // ActorMaterializer requires an implicit ActorSystem
  implicit val ec = system.dispatcher  // bindingFuture.map requires an implicit ExecutionContext

  val api = new RestApi(system, requestTimeout(config)).routes // the RestApi provides a Route
 
  implicit val materializer = ActorMaterializer()  // bindAndHandle requires an implicit materializer
  
  /**
   * Http() returns an http extension and binds the routes returned by RestApi 
   * Note that this bind call returns a future. 
   * In addition is starts an http server that waits for requests to come in.
   */
  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, host, port) //Starts the HTTP server
 
  val log =  Logging(system.eventStream, "go-ticks")
  bindingFuture.map { serverBinding =>
    log.info(s"RestApi bound to ${serverBinding.localAddress} ")
  }.onFailure { 
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}!", host, port)
      system.terminate()
  }
}

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
