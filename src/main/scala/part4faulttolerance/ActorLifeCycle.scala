package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifeCycle extends App {
  object StartChild
  class LifeCycleActor extends Actor with ActorLogging {
//    override def preStart(): Unit = log.info("I am starting")
//    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifeCycleActor], "child")
    }
  }

  val system = ActorSystem("LifecycleDemo")

  val parent = system.actorOf(Props[LifeCycleActor], "parent")
  parent ! StartChild
  parent ! PoisonPill

  /**
   * restart
   */
  object Fail
  object FailChild
  object CheckChild
  object Check

  class Parent extends Actor {
    val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info(s"${self.toString()}supervised child started ")
    override def postStop(): Unit = log.info(s"${self.toString()}supervised child stop ")

    //old actor 에서 호출됨
    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"${self.toString()} supervised actor restarting because of ${reason.getMessage}")

    //new actor 에서 호출됨
    override def postRestart(reason: Throwable): Unit =
      log.info(s"${self.toString()} supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning(s"${self.toString()} child will fail now")
        throw new RuntimeException("I failed")
      case Check =>
        log.info(s"${self.toString()} alive and kicking")
    }
  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild
}
