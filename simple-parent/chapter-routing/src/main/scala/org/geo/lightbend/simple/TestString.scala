package org.geo.lightbend.simple


object StringUtil { 
	def joiner(strings: List[String], seperator: String): String = strings.mkString(seperator)
	def joiner(strings: List[String]): String = joiner(strings, ", ") 
}

import StringUtil._ 


object TestString extends App {
  val strs = List("super" , "cali","fragelistic")
  println(List(joiner(strs)))  
}