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
import akka.actor.AddressFromURIString
import akka.actor.Address
import akka.remote.routing.RemoteRouterConfig

class RemoteRouteeTest extends TestKit(
  ActorSystem("RemoteRouteeTest"))
  with WordSpecLike with BeforeAndAfterAll {

  val timeout = 4 seconds

  val addresses = Seq(
    Address("akka.tcp", "GetLicensesSystem", "10.128.128.128", 1234),
    AddressFromURIString("akka.tcp://GetLicenseSystem@10.128.128.128:1234"))

  override def afterAll() = {
    system.terminate()
  }

  "The Router" must {
    "illustrate using Remote Routees using Config" in {
      val endProbe = TestProbe()

      /** create a romote config actor **/
      val routerRemote1 = system.actorOf(
        RemoteRouterConfig(FromConfig(), addresses).props(
          Props(new GetLicense(endProbe.ref))), "poolRouter-config")

      val s = scan3(new File("C:/bin")) {
        t =>
          // println("println file --> " + t)
          routerRemote1 ! t
          endProbe.expectMsg(timeout, t)
      }

    }
  }
  
//    "The Router" must {
//    "illustrate using Remote Routees using Code" in {
//      val endProbe = TestProbe()
//
//      /** create a romote config actor **/
//      val routerRemote1 = system.actorOf(
//        RemoteRouterConfig(FromConfig(), addresses).props(
//          Props(new GetLicense(endProbe.ref))), "poolRouter-config")
//
//      val s = scan3(new File("C:/Windows")) {
//        t =>
//          // println("println file --> " + t)
//          routerRemote1 ! t
//          endProbe.expectMsg(timeout, t)
//      }
//
//    }
//  }

}