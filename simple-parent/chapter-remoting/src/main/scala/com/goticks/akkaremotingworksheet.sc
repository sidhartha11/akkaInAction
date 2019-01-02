package com.goticks

object akkaremotingworksheet {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
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
"""                                               //> conf  : String = "
                                                  //| akka {
                                                  //|     actor {
                                                  //|         provider = "akka.remote.RemoteActorRefProvider"
                                                  //|     }
                                                  //|     remote {
                                                  //|         enabled-transports = ["akka.remote.netty.tcp"]
                                                  //|         netty.tcp {
                                                  //|             hostname = "0.0.0.0"
                                                  //|             port = 2551
                                                  //|         }
                                                  //|     }
                                                  //| }
                                                  //| "
// load the required imports and ConfigFactory
import com.typesafe.config._
import akka.actor._
val config = ConfigFactory.parseString(conf)      //> config  : com.typesafe.config.Config = Config(SimpleConfigObject({"akka":{"a
                                                  //| ctor":{"provider":"akka.remote.RemoteActorRefProvider"},"remote":{"enabled-t
                                                  //| ransports":["akka.remote.netty.tcp"],"netty":{"tcp":{"hostname":"0.0.0.0","p
                                                  //| ort":2551}}}}}))
val backend = ActorSystem("backend", config)      //> [INFO] [11/12/2018 15:10:58.105] [main] [akka.remote.Remoting] Starting remo
                                                  //| ting
                                                  //| [INFO] [11/12/2018 15:10:59.158] [main] [akka.remote.Remoting] Remoting star
                                                  //| ted; listening on addresses :[akka.tcp://backend@0.0.0.0:2551]
                                                  //| [INFO] [11/12/2018 15:10:59.166] [main] [akka.remote.Remoting] Remoting now 
                                                  //| listens on addresses: [akka.tcp://backend@0.0.0.0:2551]
                                                  //| backend  : akka.actor.ActorSystem = akka://backend/
}