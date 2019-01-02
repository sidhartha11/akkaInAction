package org.geo.akkainaction.chapterfutures

import org.slf4j.LoggerFactory
import org.apache.commons.text.RandomStringGenerator

object UtilityFunctions {

  val logger = LoggerFactory.getLogger("Utils")

  def tracestring = ">>>>>"

  def emit(message: String, show: Boolean): Unit = {
    if (show) {
      logger.info(tracestring + message)
    }
  }
  def emit(message: String): Unit = {
    emit(message, true)
  }
  def emitt(message: String, show: Boolean): Unit = {
    emit(tracestring + message, show)
  }
  def emitt(message: String): Unit = {
    emit(tracestring + message, true)
  }

  def randRange(start: Int, end: Int) = {
        java.util.concurrent.ThreadLocalRandom.current().nextInt(start,end) 
  }
  def waitSim(range: Long = 1000) = {
    val l = java.util.concurrent.ThreadLocalRandom.current().nextLong(range) + 250
    Thread.sleep(l)
  }

  def randomString(len: Int): String = {
    // Generates a 20 code point string, using only the letters a-z
    val generator: RandomStringGenerator = new RandomStringGenerator.Builder()
      .withinRange('a', 'z').build();
    generator.generate(len);

  }
  
  def latAndLon = {
    val lat = randRange(1,100)
    val lon = randRange(1,100)
    (lat,lon)
  }
}