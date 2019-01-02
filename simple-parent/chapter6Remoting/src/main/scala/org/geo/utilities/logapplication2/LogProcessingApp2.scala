package org.geo.utilities.logapplication2
import akka.actor.ActorSystem
import org.geo.utilities.Geoutils._
object LogProcessingApp2 {
 val sources = Vector(
 "file:///C:/test/log10.txt"
, "file:///C:/test/log20.txt"

)
  val system = ActorSystem("logprocessing")

  val databaseUrls = Vector(
    "mongodb://localhost:27017",
    "mongodb://localhost:27017",
    "mongodb://localhost:27017")
    
  def main(args: Array[String]) {
   emit("starting Log Processor System",true)
    /** 
     *  start the LogProcessingsupervisor actor. This simply passes
     *  new LogProcessingSupervisor(sources,databaseUrls) to Props
     *  and also passes the name defined for LogProcessingSupervisor
     */
    system.actorOf(
      LogProcessingSupervisor.props(sources, databaseUrls),
      LogProcessingSupervisor.name)
  }
}