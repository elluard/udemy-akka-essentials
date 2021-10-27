package playground

import akka.actor.ActorSystem

object Playground extends App {
  val actorSystem = ActorSystem("HelloAkka")

  println(actorSystem.name)

  def testFunction(a : Int): PartialFunction[Int, Int] = { b =>
    println(a)
    b
  }

  println(testFunction(1)(2))
}
