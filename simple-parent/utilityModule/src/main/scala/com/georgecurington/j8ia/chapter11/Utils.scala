package com.georgecurington.j8ia.chapter11
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadLocalRandom
import akka.event.jul.Logger

object Utils {
   val logger = LoggerFactory.getLogger("Utils")
   
   def peep(msg: String){
     logger.info(msg)
   }
      def peep(msg: List[String]){
     println(msg)
   }
   def waitx(time: Long){
     Thread.sleep(time)
   }
   def delay() {
     waitx(1000)
   }
	
   def getDValue(product: String): Double = {
     val d = ThreadLocalRandom.current().nextDouble() * 
     product.charAt(0) + product.charAt(1) 
     d
   }
   
   	def doSomethingElse = {
		peep(">>>>simulating doing other work")
		waitx(1000)
	}
}