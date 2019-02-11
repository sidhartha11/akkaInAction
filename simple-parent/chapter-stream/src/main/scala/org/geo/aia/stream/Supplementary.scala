package org.geo.aia.stream

import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption._
import java.nio.file.Path
import java.nio.file.Paths
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.util.ByteString
import scala.concurrent.Future
import akka.stream.IOResult
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.RunnableGraph

import akka.actor.{ActorSystem}
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory

class Supplementary {
  
}

object RunTester extends App {
   val logger = LoggerFactory.getLogger("RunTester")

  def processFileCopy(inputFile: Path,
      outputFile: Path) {
    
    /**
     * Setting up a stream involves creating a source, sink and RunnableGraph
     */
    val source: Source[ByteString, Future[IOResult]] = 
      FileIO.fromPath(inputFile) 
      
    val sink: Sink[ByteString, Future[IOResult]] = 
      FileIO.toPath(outputFile, Set(CREATE,WRITE,APPEND))
      
    val runnableGraph: RunnableGraph[Future[IOResult]] = 
      source.to(sink)
    /**
     * The RunnableGraph is executed as follows:
     * Note that the FileIO-created sources and sinks internally use 
     * blocking file I/O. The actors created for FileIO sources 
     * and sinks run on a separate dispatcher, 
     * which can be set globally with akka.stream.blocking-io-dispatcher
     * 
     */
      implicit val system = ActorSystem()
      implicit val ec     = system.dispatcher
      /**
       * ActorMaterializer converts the RunnableGraph into actors, 
       * which execute the graph
       */
      implicit val materializer = ActorMaterializer()
      
      /**
       * The actors set up a pub/sub mechanism using nonblocking back pressure.
       * This is AKKA version of reactive streams.
       * chunk read from the file, which can be set in the fromPath method 
       * and is 8 KB by default.
       */
      
      runnableGraph.run().foreach {
      result => 
        logger.info("{} , {} bytes read.",result.status, result.count)
    }
    
  }
  processFileCopy(Paths.get(args(0)),Paths.get(args(1)))
}