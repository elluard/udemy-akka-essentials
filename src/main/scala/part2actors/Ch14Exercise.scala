package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Ch14Exercise extends App {
  /**
   * Exercises
   * 1- recreate the Counter Actor with context.become and NO Mutable State
   */
  val actorSystem = ActorSystem("Exercise")

  case object Increment
  case object Decrement
  case object Print
  class CounterActor extends Actor {
    var counter : Int = 0
    override def receive: Receive = counterReceive(0)

    def counterReceive(count : Int) : Receive = {
      case Increment => context.become(counterReceive(count + 1))
      case Decrement => context.become(counterReceive(count - 1))
      case Print => println(s"[${self}] count $count")
    }
  }

  val aCounterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")

  aCounterActor ! Increment
  aCounterActor ! Print
  aCounterActor ! Increment
  aCounterActor ! Print
  aCounterActor ! Decrement
  aCounterActor ! Print

  /**
   * 2 - simplified voting system
   */

  case class Vote(candidate : String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate : Option[String])
  class Citizen extends Actor {
    override def receive: Receive = voteReceive(None)

    def voteReceive(target : Option[String]) : Receive = {
      case Vote(candidate) => context.become(voteReceive(Some(candidate)), false)
      case VoteStatusRequest => {
        sender() ! VoteStatusReply(target)
        context.unbecome()
      }
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    override def receive: Receive = normalReceive

    def normalReceive : Receive = {
      case AggregateVotes(citizens) => {
        citizens.foreach{
          _ ! VoteStatusRequest
        }
        context.become(aggregateReceive(List(), citizens.size), false)
      }
    }

    def aggregateReceive(result : List[Option[String]], remainCount : Int) : Receive = {
      case VoteStatusReply(candidate) => {
        val newResult = candidate :: result
        if(remainCount > 1) {
          context.become(aggregateReceive(newResult, remainCount - 1))
        }
        else {
          //결과 출력
          newResult.filter(_.isDefined).map(_.get).groupBy(a => a)
            .foreach { a =>
              println(s"${a._1} -> ${a._2.size}")
            }
        }
      }
    }
  }

  val alice = actorSystem.actorOf(Props[Citizen])
  val bob = actorSystem.actorOf(Props[Citizen])
  val daniel = actorSystem.actorOf(Props[Citizen])
  val charlie = actorSystem.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
}
