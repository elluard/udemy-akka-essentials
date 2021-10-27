package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.VEGETABLE


object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state = HAPPY
    override def receive: Receive = {
      case Food(CHOCOLATE) => state = HAPPY
      case Food(VEGETABLE) => state = SAD
      case Ask(_) =>
        if(state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive
    def happyReceive : Receive = {
      //discardOld 파라미터가 true 이면 이전에 사용하는 메시지 핸들러는 버림, false 이면 stack 에 쌓음, 이전 상태로 돌아갈 수 있음.
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive : Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      //discardOld 파라미터가 true 이면 이전에 사용하는 메시지 핸들러는 버림, false 이면 stack 에 쌓음, 이전 상태로 돌아갈 수 있음.
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef : ActorRef)
    case class Food(food: String)
    case class Ask(message : String) // do you want to play?
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("My kid is sad, nut as he's healthy")
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val fussyKid = actorSystem.actorOf(Props[FussyKid], "fussyKid")
  val statelessKid = actorSystem.actorOf(Props[StatelessKid], "statelessKid")
  val mom = actorSystem.actorOf(Props[Mom], "mom")

  import Mom._
  mom ! MomStart(statelessKid)
}
