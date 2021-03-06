

package aia.routing
import scala.concurrent.duration._

import akka.actor.Props
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import akka.routing.FromConfig
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.testkit.TestProbe

import DirectoryTraversal._
import java.io.File

class RounterUsingConfigTest extends TestKit(
  ActorSystem("RouterUsingConfigTest"))
  with WordSpecLike with BeforeAndAfterAll {

  val timeout = 4 seconds
  
  override def afterAll() = {
    system.terminate()
  }
 "The Router" must {
    "illustate using config for pool router" in {
      val endProbe = TestProbe()
      val router = system.actorOf(
          FromConfig.props(Props(new GetLicense(endProbe.ref))),
          "poolRouter"
          )
          
      val check = Photo("123xyz" , 60)
      router ! Photo("123xyz" , 60)
      endProbe.expectMsg(timeout, check) 
    }
}
 
 "The Router" must {
    "illustrate processing directory tree" in {
      val endProbe = TestProbe()
      val router = system.actorOf(
          FromConfig.props(Props(new GetLicense(endProbe.ref))),
          "poolRouter2"
          )
          
      val s = scan3(new File("C:/Windows")) {
      t => 
      // println("println file --> " + t)
      router ! t
      endProbe.expectMsg(timeout, t)       
    }   
          
          
//      val check = Photo("123xyz" , 60)
//      router ! Photo("123xyz" , 60)
//      endProbe.expectMsg(timeout, check) 
    }
}
 
}