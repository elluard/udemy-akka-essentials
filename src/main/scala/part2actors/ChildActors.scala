package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {
  object Parent {
    case class CreateChild(name : String)
    case class TellChild(message : String)
  }
  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) => {
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
      }
    }

    def withChild(child : ActorRef) : Receive = {
      case TellChild(message) => child forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got : $message")
    }
  }

  val actorSystem = ActorSystem("WithChild")
  val parent = actorSystem.actorOf(Props[Parent], "parent")

  parent ! CreateChild("child")
  parent ! TellChild("hey kid!")

  // child actor 는 child actor 안에서 자기의 child actor  를 만들 수 있다.
  // parent -> child -> grandchild
  // actor 계층구조에서 가장 top 에 있는 actor 를 Guardian Actor 라고 부르기도 함

  /*
    Guardian Actors (top-level)
      - /system = system guardian
      - /user = user-level guardian
      - / = the root guardian
   */

  //path 로 액터 선택하기
  val childSelection = actorSystem.actorSelection("/user/parent/child")
  childSelection ! "I found you!"

  //잘못된 path 로 액터를 선택할 경우, 메시지를 보내도 그냥 버려진다.
  val wrongchildSelection = actorSystem.actorSelection("/user/parent/wrongChild")
  wrongchildSelection ! "I found you!" //메시지는 그냥 버려짐, 아무일도 안일어남.

  /**
   * Danger!
   * Never Pass mutable actor state, or the `this` reference, to child actors`
   * Never in your life
   */


}
