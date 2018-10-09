package org.geo.utilities
import java.io._
import java.net._

import scala.collection.mutable.ArrayBuffer
object Geoutils {

  def readFile(uri: String): Unit = {
    // val url = new URL("file:///Users/al/Desktop/50-Shades-Quotes.txt")
    val url = new URL(uri)

    val in = new BufferedReader(new InputStreamReader(url.openStream))
    val buffer = new ArrayBuffer[String]()
    var inputLine = in.readLine
    while (inputLine != null) {
      if (!inputLine.trim.equals("")) {
        buffer += inputLine.trim
      }
      inputLine = in.readLine
    }
    in.close
  }
  
  def getFile(uri: String): File = {
    new File(uri)
  }
}