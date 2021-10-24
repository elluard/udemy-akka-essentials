package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {
  //creating threads on JVM

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })
  aThread.start()
  aThread.join() //join 함수가 호출되면, 이 쓰레드가 종료된 후에 메인 쓰레드가 종료된다.

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGoodbye.start()

  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    // withdraw is not atomic, 쓰레드 동시성 문제가 발생한다.
    def withdraw(money: Int) = this.amount -= money

    //아래 함수처럼 synchronized 함수로 락을 걸어주거나
    //여러 쓰레드에서 접근하는 변수에 @volatile annotation 을 걸어준다.
    //(atomic read 만 지원한다, write 는 여전히 non-atomic 임을 주의, write 에서는 synchronized 함수를 사용해야 한다.)
    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  // inter-thread communication on JVM
  // wait - notify mechanism

  import scala.concurrent.ExecutionContext.Implicits.global
  // Scala futures
  val future = Future {
    // 긴 시간동안 걸리는 작엄임을 가정한다. - 다른 쓰레드에서 실행됨
    42
  }

  // callbacks

  future.onComplete {
    case Success(42) => println("Success")
    case Failure(exception) => println("Failure")
  }

  val aProcessFuture = future.map(_ + 1) // future 의 결과는 43이 됨
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  } //future 의 결과는 44

  val filteredFuture = future.filter(_ % 2 == 0) //NoSuchElementException

  //for comprehensions

  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning
}
