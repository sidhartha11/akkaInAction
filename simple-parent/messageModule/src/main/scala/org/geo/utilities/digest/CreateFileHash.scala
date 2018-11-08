package org.geo.utilities.digest

import java.security.MessageDigest
import java.nio.file.Paths
import java.nio.file.Files
import javax.xml.bind.DatatypeConverter
import java.io.File

object CreateFileHash extends App {
  def md = MessageDigest.getInstance("MD5")
  def file1 = "C:\\newcannon\\newebay_5\\image_101.jpg"
  def file2 = "C:\\newcannon\\newebay_5\\image_102.jpg"
  def base = "C:\\newcannon\\newebay_5"


  def fileStream(dir: File): Stream[File] =
  if (dir.isDirectory)
    Option(dir.listFiles)
      .map(_.toList.sortBy(_.getName).toStream.flatMap(file => file #:: fileStream(file)))
      .getOrElse {
        println("exception: dir cannot be listed: " + dir.getPath)
        Stream.empty
      }
  else Stream.empty
  
  def createHash(name1: String, name2: String) = {
    val md: MessageDigest = MessageDigest.getInstance("MD5")
    val begin = System.currentTimeMillis()
    md.update(Files.readAllBytes(Paths.get(name1)))
    var digest = md.digest()
    var myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase
    val l = System.currentTimeMillis() - begin
    println("myChecksum = %s,l=%d".format(myChecksum,l))
   
    md.reset()
    
    md.update(Files.readAllBytes(Paths.get(name2)))
    digest = md.digest()
    myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase
    println("myChecksum = %s".format(myChecksum))

  }
  
  // createHash(file1,file1)
  val fls = fileStream(new File(base))
  for ( f <- fls.take(fls.length) ) {
    println(f)
  }
  println("fls.length = %d".format(fls.length))
}