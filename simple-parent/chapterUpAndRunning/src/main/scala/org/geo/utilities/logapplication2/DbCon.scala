package org.geo.utilities.logapplication2

import org.slf4j.LoggerFactory

class DbCon(url: String) {
  /**
   * Writes a map to a database.
   * @param map the map to write to the database.
   * @throws DbBrokenonnectionException when the connection is broken. 
   * it might be broken later
   * @throws DbNodeDownException when the database Node has been removed 
   * from the database cluster
   */
  
  val logger = LoggerFactory.getLogger(classOf[DbCon])
  logger.debug(myName + " Constructor")
  def write(map: Map[Symbol, Any]): Unit = {
    logger.debug(myName + " in write")
    logger.debug("%s map=%s".format(myName,map))
  }
  def close(): Unit = {
     logger.debug(myName + " in close")
  }
  def myName = "DbCon"
}