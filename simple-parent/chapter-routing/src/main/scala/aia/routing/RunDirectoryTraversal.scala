package aia.routing

import DirectoryTraversal._
import java.io.File

object RunDirectoryTraversal extends App {

  def testGetList {
    val s = scan2(new File("C:/Windows"))
    s foreach {
      fl =>
        println("fl=%s".format(fl))
    }
  }

  def processFiles {
    val s = scan3(new File("C:/Windows")) {
      t => println("println file --> " + t)
    }
  }

  processFiles
}