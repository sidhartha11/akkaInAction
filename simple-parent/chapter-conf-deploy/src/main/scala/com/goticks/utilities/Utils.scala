package com.goticks.utilities

import org.slf4j.LoggerFactory

object Utils {
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
   def emitt(message: String,show: Boolean ): Unit = {
    emit(tracestring + message, show)
  }
     def emitt(message: String): Unit = {
    emit(tracestring + message, true)
  }
  
}