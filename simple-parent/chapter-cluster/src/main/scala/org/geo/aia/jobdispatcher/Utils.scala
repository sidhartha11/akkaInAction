package org.geo.aia.jobdispatcher
import org.slf4j.LoggerFactory

object Utils {
  val logger = LoggerFactory.getLogger("Utils")
  def peep(msg: String) {
    logger.info(msg)
  }
}