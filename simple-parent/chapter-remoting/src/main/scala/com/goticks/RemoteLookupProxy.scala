package com.goticks

import akka.actor._
import akka.actor.ActorIdentity
import akka.actor.Identify

import scala.concurrent.duration._

import com.goticks.utilities.Utils._


class RemoteLookupProxy(path: String)
  extends Actor with ActorLogging {

  emitt(nm + " starting up")
  emitt(nm + " setting timeout to %s".format(3 seconds))
  context.setReceiveTimeout(3 seconds)
  emitt(nm + " calling sendIdentifyRequest()")
  sendIdentifyRequest()

  def nm="RemoteLookupProxy"
  def sendIdentifyRequest(): Unit = {
    emitt(nm + " enter sendIdentifyRequest")
    emitt(nm + " calling context.actorSelection(%s)".format(path))
    val selection = context.actorSelection(path)
    emitt(nm + " sending Identify(%s) message to %s".format(path,selection))
    selection ! Identify(path)
  }

  def receive = identify

  def identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      emitt(nm + " received ActorIdentity message for %s".format(actor))
      context.setReceiveTimeout(Duration.Undefined)
      emitt(nm + " switching to active state ")
      log.info("switching to active state")
      context.become(active(actor))
      emitt(nm + " watching actorRef %s".format(actor))
      context.watch(actor)

    case ActorIdentity(`path`, None) =>
      emitt(nm + " actor with path %s not available".format(path))
      log.error(s"Remote actor with path $path is not available.")

    case ReceiveTimeout =>
      emitt(nm + " timeout message received, resending sendIdentifyRequest()")
      sendIdentifyRequest()

    case msg: Any =>
      emitt(nm + " got a message before the remote actor was ready")
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated.")
      log.info("switching to identify state")
      context.become(identify)
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest()

    case msg: Any => 
      emitt(nm + " msg gotton: %s".format(msg))
      emitt(nm + " got new message in active state, will forward to %s".format(msg))
      actor forward msg
  }
}
