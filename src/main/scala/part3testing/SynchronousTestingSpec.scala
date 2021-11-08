package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.Duration

/**
 * akka 의 경우, ! 연산자를 사용하여 테스트를 하면, message 가 비동기적으로 전달된다.
 * 로직 테스트를 위해 결과의 확인이 바로 필요할 때, synchronous test 를 통해 가능하다.
 */
class SynchronousTestingSpec extends AnyWordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("SynchronousTestingSpec")

  override def afterAll(): Unit = {
    system.terminate()
  }

  import SynchronousTestingSpec._
  "A counter" should {
    "synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc // counter has ALREADY received the message
      assert(counter.underlyingActor.count == 1)
    }

    "synchronously increase its counter at the call of the receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      assert(counter.underlyingActor.count == 1)
    }

    "work on the calling thread dispatcher" in {
      // withDispatcher(CallingThreadDispatcher.Id) 가 빠진경우, 아래 테스트는 실패함
      // 그 이유는 메시지 전달이 비동기적으로 일어나고, akka 의 매커니즘 대로 동작하기 때문에 타임아웃이 발생한다.
      // 데드락 문제가 발생할 수도 있음, 주의할 것.
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()

      probe.send(counter, Read)
      probe.expectMsg(Duration.Zero, 0)
    }
  }
}

object SynchronousTestingSpec {
  case object Inc
  case object Read

  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }
  }
}