package org.geo.aia.jobdispatcher

import org.slf4j.LoggerFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.actor.{ Props, ActorRef }
import scala.concurrent.Future
import scala.util.Success

/** The Default Execution Context **/
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import JobReceptionist._


/**
 * This is the boot application for the Clustering Example.
 * This code will start the master node up and the job receptionist node.
 * Command contains the configuration file name and the listening port number for
 * the actor system.
 * prog configfilename ipaddress port
 * Note:
 * THe ipaddress is not used in this application. THat is still acquired from
 * the actual config file.
 * Configuration:
 * JobReceptionist and JobMaster will run on the master node.
 * Both JobMaster and JobWorker actors are created dynamically,
 * on demand. Whenever a JobReceptionist receives a Job-Request,
 * it spawns a JobMaster for the Job and tells it to start work
 * on the job. The JobMaster creates JobWorkers remotely
 * on the worker role nodes.
 * 
 * PORT SETUP FOR EXAMPLE
 * java -DPORT=2551 \
 *    -Dconfig.resource=/seed.conf \
 *   -jar target/words-node.jar
 * java -DPORT=2554 \
 *   -Dconfig.resource=/master.conf \
 *   -jar target/words-node.jar
 *  java -DPORT=2555 \
 *    -Dconfig.resource=/worker.conf \
 *    -jar target/words-node.jar
 *  java -DPORT=2556 \
 *    -Dconfig.resource=/worker.conf \
 *   -jar target/words-node.jar
 *   
 *   geomaster.conf 127.0.0.1 2554
 *   geoseed.conf 127.0.0.1 2551
 *   geoworker.conf 127.0.0.1 2555
 *   geoworker.conf 127.0.0.1 2556
 *
 *
 */
object Main extends App {
  val logger = LoggerFactory.getLogger("Main")

  /**
   * let args[0] = conf file
   * let args[1] = host
   * let args[2] = port
   */
  def portpattern = "akka.remote.netty.tcp.port"
  def hostpattern = "akka.remote.netty.tcp.host"
  def loadConfigFile(args: Array[String]): Option[Config] = {

    logger.info("args = {}",args)
    /**
     * check args for nullity
     */

    if (args.isEmpty == true) {
      logger.error("usage: prog configfile hostname port")
      None
    } else {
      val configfile = args(0)
      val host = args(1)
      val port = args(2)
      val seedConfig =
        ConfigFactory.load(configfile).resolve()

      /**
       * override config port value with command line argument
       */
      val portpattern = "akka.remote.netty.tcp.port"
      val hostpattern = "akka.remote.netty.tcp.host"
      logger.info("overriding config %s with %s".format(portpattern, port))

      var newConfig =
        seedConfig.withValue(
          portpattern,
          ConfigValueFactory.fromAnyRef(port))
      logger.info("new val = {}", newConfig.getAnyRef(portpattern))

      logger.info("overriding config %s with %s".format(portpattern, port))

      newConfig =
        newConfig.withValue(
          hostpattern,
          ConfigValueFactory.fromAnyRef(host))
      logger.info("new val = {}", newConfig.getAnyRef(hostpattern))
      Some(newConfig)
    }
  }

  def startActorSystem(config: Config): Option[ActorRef] = {
    /**
     * create an actor system called "words" using input config
     */
    val system = ActorSystem("words", config)
    /**
     * Look up the roles in the config that are present for this actorsystem
     */
    logger.info("Starting node with roles: %s".format(Cluster(system).selfRoles))

    /**
     * programatically look up the roles associated with the config akka.cluster.roles
     * if the set of roles contains "master" then execute the block of code given
     * when the Cluster is considered to be up and running.
     */
    val roles = system.settings
      .config
      .getStringList("akka.cluster.roles")
      
      /**
       * If this is a master node configuration, then we will start the Receptionist
       * actor and return it's ActorRef, 
       * otherwise we will just return an Option of None
       */
    var receptionist: Option[ActorRef] = null
    if ( roles.contains("master")) {
      logger.info("checking to see if seeds are up and running:{}", system)
      /** check to see if enough seeds are running so that we can bring up receptionist **/
      Cluster(system).registerOnMemberUp {
        println("registerOnMemberUp executing")
        receptionist = Some(system.actorOf(Props[JobReceptionist], "receptionist"))
        val text = List("this is a test", "of some very naive word counting", "but what can you say", "it is what it is")
      receptionist.get ! JobRequest("the first job", (1 to 10).flatMap(i => text ++ text).toList)
      }
      
    } else {
      /** this is just a simple seed node coming up **/
      logger.info("seed block executing")
      receptionist = None
    }
    receptionist
  }

  /** load config file **/
  val conf = loadConfigFile(args).get
  val receptionlist = startActorSystem(conf)
  logger.info("receptionlist = {}",receptionlist )

}