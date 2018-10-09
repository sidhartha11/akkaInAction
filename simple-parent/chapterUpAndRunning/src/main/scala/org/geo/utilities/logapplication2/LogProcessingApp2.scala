package org.geo.utilities.logapplication2
import akka.actor.{ ActorSystem }
object LogProcessingApp2 {
 val sources = Vector(
 "file:///C:/test/allxa"
, "file:///C:/test/testHtml.html"
//,"file:///C:/test/xae"
//,"file:///C:/test/xaf"
//,"file://C:/test/xag"
//,"file://C:/test/xah"
//,"file://C:/test/xai"
//,"file://C:/test/xaj"
//,"file://C:/test/xak"
//,"file://C:/test/xal"
//,"file://C:/test/xam"
//,"file://C:/test/xan"
//,"file://C:/test/xao"
//,"file://C:/test/xap"
//,"file://C:/test/xaq"
//,"file://C:/test/xar"
//,"file://C:/test/xas"
//,"file://C:/test/xat"
//,"file://C:/test/xau"
//,"file://C:/test/xav"
//,"file://C:/test/xaw"
//,"file://C:/test/xax"
//,"file://C:/test/xay"
//,"file://C:/test/xaz"
)
  val system = ActorSystem("logprocessing")

  val databaseUrls = Vector(
    "http://mydatabase1",
    "http://mydatabase2",
    "http://mydatabase3")
    
  def main(args: Array[String]) {
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