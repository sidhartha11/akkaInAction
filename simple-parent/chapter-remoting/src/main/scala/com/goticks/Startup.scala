package com.goticks

import com.goticks.utilities.Utils._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.event.Logging

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import scala.util.{Success, Failure}

import akka.stream.ActorMaterializer

trait Startup extends RequestTimeout {
  def startup(api: Route)(implicit system: ActorSystem) = {
    val host = system.settings.config.getString("http.host") // Gets the host and a port from the configuration
    val port = system.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)
      (implicit system: ActorSystem) = {
    
    val log = Logging(system.eventStream, "go-ticks")
    log.info(tracestring + "called startHttpServer on host {} and port {}",host,port)
    implicit val ec = system.dispatcher  //bindAndHandle requires an implicit ExecutionContext
    implicit val materializer = ActorMaterializer()
    val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, host, port) //Starts the HTTP server
   
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress} ")
    }.onComplete { 
      case Success(suc) => 
        log.info(tracestring + "Success Completion for{}:{}:{}!", host, port,suc)
        //system.terminate()
      case Failure(ex)  =>
        log.error(ex, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
  
    def startHttpServerDeprecated(api: Route, host: String, port: Int)
      (implicit system: ActorSystem) = {
    implicit val ec = system.dispatcher  //bindAndHandle requires an implicit ExecutionContext
    implicit val materializer = ActorMaterializer()
    val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, host, port) //Starts the HTTP server
   
    val log = Logging(system.eventStream, "go-ticks")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress} ")
    }.onFailure { 
      case ex: Exception =>
        log.error(ex, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
}
