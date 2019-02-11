package org.geo.aia.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem,Props }
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.Cluster
import akka.cluster.MemberStatus


import akka.cluster.ClusterEvent.CurrentClusterState

import com.typesafe.config.ConfigFactory
import akka.cluster.ClusterEvent.{MemberUp,MemberExited,MemberRemoved,UnreachableMember,ReachableMember}

class ClusterDomainEventListener extends Actor with ActorLogging {
  
  /**
   * Subscribe to ClusterDomainEvents
   */
  Cluster(context.system).subscribe(self,classOf[ClusterDomainEvent])
  
  def receive = {
    case MemberUp(member)    =>  log.info(s"$member Up.")
    case MemberExited(member) => log.info(s"$member EXITED.")
    case MemberRemoved(m, previousState) => 
      if (previousState == MemberStatus.Exiting) {
        log.info(s"Member $m gracefully exited, REMOVED.")
      } else {
        log.info(s"$m downed after unreachable, REMOVED.")
      }
    case UnreachableMember(m) => log.info(s"$m UNREACHABLE")
    case ReachableMember(m)   => log.info(s"$m REACHABLE")
    case s: CurrentClusterState => log.info(s"cluster state: $s")
  }
  
   override def postStop(): Unit = {
     Cluster(context.system).unsubscribe(self)
     super.postStop()
   }
}

  object TestThis extends App {
  val config = ConfigFactory.load("simpleseed.conf").resolve()
  val system = ActorSystem("words", config)
  val actor = system.actorOf(Props[ClusterDomainEventListener],"watcher")
   }