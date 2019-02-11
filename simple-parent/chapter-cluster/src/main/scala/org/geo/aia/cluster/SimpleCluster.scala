package org.geo.aia.cluster

import com.typesafe.config.ConfigFactory

import akka.actor.{ ActorSystem }
import org.slf4j.LoggerFactory
import akka.cluster.Cluster
import akka.actor.Address
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory

object SimpleCluster extends App {
  val logger = LoggerFactory.getLogger("SimpleCluster")

  def loadConfig(args: Array[String]): Option[Config] = {

    if (args.isEmpty == true) {
      None
    } else {
      val seedConfig =
        ConfigFactory.load("simpleseed").resolve()

      /**
       * override config port value with command line argument
       */
      val pattern = "akka.remote.netty.tcp.port"
      val s = "overriding config %s with %s".format(pattern, args(0))
      logger.info("overriding {}", s)

      val newConfig =
        seedConfig.withValue(
          pattern,
          ConfigValueFactory.fromAnyRef(args(0)))
      logger.info("new val = {}", newConfig.getAnyRef(pattern))
      Some(newConfig)
    }
  }

  def startSeed(actorsystem: String, config: Config): ActorSystem = {
    logger.info("starting seed")
    val seedSystem = ActorSystem("words", config)
    seedSystem
  }

  def bringUpCluster(args: Array[String]): ActorSystem = {
    /**
     * Get the configuration and use the command line PORT number
     */
    val config = loadConfig(args).get
    /**
     * Bring up the cluster ActorSystem with that port
     */
    val seed = startSeed("simpleseed", config)
    seed
  }
  
    def bringUpClusterWithWatcher(args: Array[String]): ActorSystem = {
    /**
     * Get the configuration and use the command line PORT number
     */
    val config = loadConfig(args).get
    /**
     * Bring up the cluster ActorSystem with that port
     */
    val seed = startSeed("simpleseed", config)
    seed
  }

  def bringUpClusterAndTakeLeaderDown(args: Array[String]){
    val system = bringUpCluster(args)
    Thread.sleep(100000)
    logger.info ("taking the leader down manually")
    takeSeedDownManually(system,2551)
  }
  def takeSeedDownManually(seed: ActorSystem, port: Int) {
    logger.info("taking seed {} down manually", port)
    val address = Address("akka.tcp", "words", "127.0.0.1", port)
    Cluster(seed).down(address)
  }

  def startSeedAndLeave(args: Array[String], timeout: Long) {

    val seed = bringUpCluster(args)
    Thread.sleep(timeout)
    /**
     * now lets leave the cluster
     */
    logger.info("leaving cluster")
    val address = Cluster(seed).selfAddress
    logger.info("got address {} of seed", address)
    Cluster(seed).leave(address)
  }
  
  /**
   * Bring Up A Cluster using port from command line 
   */
  bringUpCluster(args)
  
  /**
   * Test taking down a crashed leader manually
   * First it has to belong to the cluster to perform this function.
   * SO , the function below will simulate it by waiting 1 minute before
   * it tries to take the FAILED node down properly. 
   */
  // bringUpClusterAndTakeLeaderDown(args)
}