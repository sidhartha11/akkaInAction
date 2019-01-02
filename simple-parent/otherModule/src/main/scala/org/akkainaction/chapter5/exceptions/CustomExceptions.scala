package org.akkainaction.chapter5.exceptions

object CustomExceptions {
  
  @SerialVersionUID(1L)
  class TrafficServiceException(msg: String)
  extends Exception(msg) with Serializable
}