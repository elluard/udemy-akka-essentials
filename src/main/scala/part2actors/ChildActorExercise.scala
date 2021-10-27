package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorExercise extends App {
  object WordCounterMaster {
    case class Initialize(nChildren : Int)
    case class WordCountTask(srcActor : ActorRef, text: String)
    case class WordCountReply(srcActor: ActorRef, count : Int)
  }

  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(nChildren) =>
        val childActors = (0 until nChildren).map{ a =>
          context.actorOf(Props[WordCounterWorker], s"Worker${a}")
        }.toArray
        context.become(wordCountReceive(childActors, 0))
    }

    def wordCountReceive(childActors: Array[ActorRef], callCount : Int) : Receive = {
      case WordCountTask(srcActor, text) => {
        val actor = childActors(callCount)
        actor ! WordCountTask(srcActor, text)
        context.become(wordCountReceive(childActors, (callCount + 1) % childActors.length))
      }
      case WordCountReply(srcActor, count) => {
        srcActor ! WordCountReply(srcActor, count)
      }
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(srcActor, text) => {
        println(s"${self.path} - word ${text} count ${text.length}")
        sender() ! WordCountReply(srcActor, text.length)
      }
    }
  }

  case class StartWordCount(master : ActorRef)
  class Requester extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case StartWordCount(master) => {
        master ! Initialize(3)
        master ! WordCountTask(self, "Hello")
        master ! WordCountTask(self, "I am an actor")
        master ! WordCountTask(self, "Here is Korea")
        master ! WordCountTask(self, "It is cold")
        master ! WordCountTask(self, "round robin")
      }
      case WordCountReply(srcActor, count) =>
        println(s"count is ${count}")
    }
  }

  val actorSystem = ActorSystem("WordCounter")
  val requester = actorSystem.actorOf(Props[Requester])
  val wordCounterMaster = actorSystem.actorOf(Props[WordCounterMaster])

  requester ! StartWordCount(wordCounterMaster)
}
