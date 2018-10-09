package org.geo.bdd.src.lifecycleevents

/** import actor classes **/
import akka.actor.{Actor,ActorRef,ActorLogging,Terminated}

class DbWatcher(dbWriter: ActorRef)  extends Actor with ActorLogging
{
  /** use this actots context to monitor another actor **/
  /** context is a variable inherited from Actor **/
  context.watch(dbWriter)
  /** this actor will wait for the monitored actor to stop **/
  
  def receive = {
    case Terminated(actorRef) => 
      log.warning("Actor {} terminated",actorRef)
  }
}