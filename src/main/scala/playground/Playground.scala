package playground

import akka.actor.ActorSystem

object Playground extends App {
  val actorSystem = ActorSystem("HelloAkka")

  type Receive = PartialFunction[Any, Unit]

  println(actorSystem.name)

  def testFunction(a : Any): PartialFunction[Any, Unit] = { _ =>
    println(a)
  }

  def topFunction(partialFunction: Receive): Unit = {
    println("topFunction")
    partialFunction()
  }

  topFunction(testFunction(1))
}
