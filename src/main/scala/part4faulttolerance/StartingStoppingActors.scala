package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}
import akka.dispatch.sysmsg.Terminate

object StartingStoppingActors extends App {
  val system = ActorSystem("StoppingActorsDemo")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]) : Receive = {
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))

      case StopChild(name) =>
        log.info(s"Stopping child with the name ${name}")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        //이경우 모든 child actor 들을 종료 한 후, 자기 자신을 종료한다.
        context.stop(self)
      case message =>
        log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * method #1 - using context.stop
   */
  import Parent._
//  val parent = system.actorOf(Props[Parent], "parent")
//  parent ! StartChild("child1")
//  val child = system.actorSelection("/user/parent/child1")
//  child ! "hi kid"
//
//  parent ! StopChild("child1")
//
////  for(_ <- 1 to 50) child ! "are you still there?"
//  parent ! StopChild("child2")
//
//  val child2 = system.actorSelection("/user/parent/child2")
//  child2 ! "Hi, second child"
//  parent ! Stop
//  for (_ <- 1 to 10) parent ! "parent, are you still there?" //should not be received
//  for(i <- 1 to 100) child2 ! s"[$i]second kid, are you still alive?"

  /**
   * method #2 using speecial messages, 아래 메시지를 보냈을 경우, 액터는 바로 종료된다.
   */
//  val looseActor = system.actorOf(Props[Child])
//  looseActor ! "hello, loose actor"
//  looseActor ! PoisonPill
//  looseActor ! "loose actor, are you still there?"
//
//  //kill 메시지로 액터를 종료할 떄는 ActorKilledException 예외가 throw 된다.
//  val abruptlyTerminatedActor = system.actorOf(Props[Child])
//  abruptlyTerminatedActor ! "you are about to be terminated"
//  abruptlyTerminatedActor ! Kill
//  abruptlyTerminatedActor ! "you have been terminated"

  /**
   * Death watch
   */

  class Watcher extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started and watching shild $name")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"The reference that I'm watching $ref has been stopped")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(500)

  watchedChild ! PoisonPill
}
