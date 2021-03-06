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
