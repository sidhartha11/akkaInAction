package org.geo.utilities.exceptions

import java.io.File

object CustomExceptions {
  
  @SerialVersionUID(1L)
  class DiskError(msg: String) extends Error(msg) with Serializable 
  
  @SerialVersionUID(1L)
  class CorruptedFileException(msg: String, val file: File)
  extends Exception(msg) with Serializable 
  
  @SerialVersionUID(1L)
  class DbNodeDownException(msg: String)
  extends Exception(msg) with Serializable 
  
  @SerialVersionUID(1L)
  class DbBrokenConnectionException(msg: String)
  extends Exception(msg) with Serializable 
}