package part5infra

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._
import scala.language.postfixOps

object TimersSchedulers extends App {
  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simpleActor")

//  implicit val executionContext = system.dispatcher
  import system.dispatcher
//  system.scheduler.scheduleOnce(1 second) {
//    simpleActor ! "reminder"
//  }
//
//  val routine : Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
//    simpleActor ! "heartbeat"
//  }
//
//  system.scheduler.scheduleOnce(5 seconds) {
//    routine.cancel()
//  }

  /**
   * Exercise : implement a self-closing actor
   *
   *  - if the actor receives as message(anything), you have 1 second to send it another message
   *  - if the time window expires, the actor will stop itself
   *  - if you send another message, the time window is reset
   */

  class SelfClosingActor extends Actor with ActorLogging {
    def createTimeoutWindow() : Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        // 이 코드 안에 액터 라이프사이클 관련 코드를 넣지 말 것, 쓰레드 동시성으로 인해 문제가 생길 수 있음.
        self ! "timeout"
      }
    }

    override def postStop(): Unit = {
      log.info("Stopping Actor")
    }

    override def receive: Receive = {
      case message =>
        log.info(s"received : ${message}")
        val timer = createTimeoutWindow()
        context.become(selfClose(timer))
    }

    def selfClose(timer: Cancellable) : Receive = {
      case "timeout" =>
        context.stop(self)
      case message =>
        log.info(s"received : ${message}")
        timer.cancel()
        val newTimer = createTimeoutWindow()
        context.become(selfClose(newTimer))
    }
  }

  val selfClose = system.actorOf(Props[SelfClosingActor], "selfClosing")

//  val cancellable = system.scheduler.schedule(1 seconds, 0.5 seconds) {
//    selfClose ! "new message"
//  }
//
//  system.scheduler.scheduleOnce(10 seconds) {
//    cancellable.cancel()
//  }

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  var timerHeartbeatActor: ActorRef = system.actorOf(Props[TimerBasedHeartbeatActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerHeartbeatActor ! Stop
  }
}
