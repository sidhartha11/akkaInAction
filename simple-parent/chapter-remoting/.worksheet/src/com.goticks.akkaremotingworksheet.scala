package com.goticks

object akkaremotingworksheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(95); 
  println("Welcome to the Scala worksheet");$skip(265); 
  val conf = """
akka {
    actor {
        provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
            hostname = "0.0.0.0"
            port = 2551
        }
    }
}
"""
// load the required imports and ConfigFactory
import com.typesafe.config._
import akka.actor._;System.out.println("""conf  : String = """ + $show(conf ));$skip(141); 
val config = ConfigFactory.parseString(conf);System.out.println("""config  : com.typesafe.config.Config = """ + $show(config ));$skip(45); 
val backend = ActorSystem("backend", config);System.out.println("""backend  : akka.actor.ActorSystem = """ + $show(backend ))}
}
