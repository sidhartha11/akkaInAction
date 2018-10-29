package org.akkainaction.chapter5.entities

case class Person(firstName: String, lastName: String, age: Int)

case class Users(id: Long, username: String, password: String, enabled: Boolean)
trait RequestUser {
  
}
class Gusers extends RequestUser {
  var id: Long = _
  var username: String = _ 
  var password: String = _
  var enabled: Boolean = _ 
  override def toString = s"Gusers:[%d,%s, %s, %b]".format(id,username,password,enabled)
}