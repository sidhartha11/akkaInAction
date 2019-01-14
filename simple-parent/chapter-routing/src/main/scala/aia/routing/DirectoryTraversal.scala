package aia.routing

import java.io.File

object DirectoryTraversal {

  def scan(file: File): List[File] = {

    @scala.annotation.tailrec
    def sc(acc: List[File], files: List[File]): List[File] = {
      files match {
        case Nil => acc
        case x :: xs => {
          val canRead = x.canRead()
          canRead match {

            case true =>
              x.isDirectory() match {
                case false =>
                  sc(x :: acc, xs)
                case true =>

                  /** check for exception and just ignore if there is one **/
                  val ls: List[File] = try {
                    x.listFiles.toList
                  } catch {
                    case e: Exception =>
                      println(x + " --> exception caught:" + e)
                      List()
                    case _: Throwable =>
                      List()
                  }
                  if (ls.isEmpty)
                    sc(acc, xs)
                  else
                    sc(acc, xs ::: ls)
              }
            case false =>
              sc(acc, xs)
          } // can read
        } // case x::xs
      }
    }
    sc(List(), List(file))
  } // end scan

  /**
   * This function will try to get the list of files associated
   * with the directory, x. If it gets an error , it will simply
   * return the xs List[File] parameter; otherwise it will append 
   * xs the the beginning of the list. 
   */
  def check(x: File, xs: List[File]): List[File] = {
    val ls: List[File] = try {
      x.listFiles.toList
    } catch {
      case e: Exception =>
        println(x + " --> exception caught:" + e)
        List()
      case _: Throwable =>
        List()
    }
    if (ls.isEmpty)
      xs
    else
      xs ::: ls
  }
  
  /**
   * scan3 is a recursive function used to recursively process a 
   * directory tree and all of the subdirectories of the true.
   * It takes a functional parameter that will process each file. 
   */
    def scan3(file: File)(f: File => Unit): Unit = {
    @scala.annotation.tailrec
    def sc(files: List[File]): Unit = {
      files match {
        case Nil => 
          /**
           * If we get Nil, we have reached the end of the list.
           */
          println("finished")
        case x :: xs => {
          x.isDirectory() match {
            case false =>
              /**
               * If the file being inspected is a simple file, not a directory,
               * then we just process it and recursively call with the tail. 
               */
              f(x)
              sc(xs)
            case true =>
              /**
               * If the file being inspected is a directory, then we need
               * to expand it and append it to the current List that is being
               * processed. And then recursively call the function with the 
               * expanded list. If an error occurs such as Windows No-Permission
               * error , we just recursively call with the current tail. The 
               * utlity function check handles that for us. 
               */
              val ls = check(x, xs)
              sc(ls)
          }
        } // case x::xs
      }
    }
    sc(List(file))
  } // end

  def scan2(file: File): List[File] = {
    @scala.annotation.tailrec
    def sc(acc: List[File], files: List[File]): List[File] = {
      files match {
        case Nil => acc
        case x :: xs => {
          x.isDirectory() match {
            case false =>
              sc(x :: acc, xs)
            case true =>
              val ls = check(x, xs)
              sc(acc, ls)
          }
        } // case x::xs
      }
    }
    sc(List(), List(file))
  } // end scan2


}