package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  // setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._
  "A simple actor" should {
    "send back the same message" in {
      //testing scenario
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)
    }
  }

  "A blackhole actor" should {
    "send back some message" in {
      val blackHole = system.actorOf(Props[BlackHole])
      val message = "hello, test"
      blackHole ! message

      expectNoMessage(1 second) // akka.test.single-expect-default 에 응답 타임아웃 값이 들어가있음
    }
  }

  //message assertions
  "A lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into uppercase" in {
      labTestActor ! "I love Akka"
      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"

      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in a diifferent way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) // Seq[Any]

      //free to do more complicated assertions
    }

    "reply with coll tech in a fancy way"  in {
      labTestActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" =>  //only care that the PF is defined
        case "Akka" =>
      }
    }
  }
}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}
