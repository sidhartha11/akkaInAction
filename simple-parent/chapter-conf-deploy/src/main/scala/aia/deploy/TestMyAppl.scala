package aia.deploy

//import com.typesafe.config.ConfigFactory

import com.goticks.utilities.Utils._
import com.typesafe.config.{Config, ConfigFactory}
/**
 * To Load From A Regular File:
 * val myConfigFile = new File("path/to/myconfig.conf")
 * val fileConfig = ConfigFactory.parseFile(myConfigFile).getConfig("myconfig")
 * val config = ConfigFactory.load(fileConfig)
 */
object TestMyAppl extends App {
  
  def test1 = {
      val config = ConfigFactory.parseResources("MyAppl.conf").resolve()
   val applicationVersion = config.getInt("MyAppl.version")
   val databaseCfg = config.getConfig("MyAppl.database") 
   val databaseConnectString = databaseCfg.getString("connect")
   emitt("applicationVersion=%s,databaseConnectString=%s".format(applicationVersion,databaseConnectString))
  }
   val config = ConfigFactory.load("MyAppl.conf").resolve()
   val applicationVersion = config.getInt("MyAppl.version")
   val databaseCfg = config.getString("MyAppl.database.connect") 
   emitt("applicationVersion=%s,databaseConnectString=%s".format(applicationVersion,databaseCfg))
}