package org.geo.scalainaction.explanations

import akka.actor.ActorSystem
/**
 * 
 * ActorSystem must be created before any actor can be
 * created. Afterwhich, actors themselves can create other
 * actors
 * The 12.12 source showing the apply method for 
 * creating an Actor System:
 *   
 *  def apply(name: String, config: Option[Config] = None, classLoader: Option[ClassLoader] = None, defaultExecutionContext: Option[ExecutionContext] = None): ActorSystem = {
 *  val cl = classLoader.getOrElse(findClassLoader())
 *    val appConfig = config.getOrElse(ConfigFactory.load(cl))
 *    new ActorSystemImpl(name, appConfig, cl, defaultExecutionContext, None).start()
 *  }
 */
object whatactorsystem {
  /** create implicit actor system **/
  implicit val system = ActorSystem()
  
  /** get an execution context **/
  implicit val ec = system.dispatcher
  
  def main(args: Array[String]) {
    println("running but doing nothing")
    system.terminate()
  }
}