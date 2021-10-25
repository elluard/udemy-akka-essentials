package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App {
  val partialFunction : PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val aFunction : (Int) => Int = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  var pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(60) //결과는 9000
  //pfChain(457) //Exception

  //type aliases

  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive : ReceiveFunction = {
    case 1 => println("hello")
    case _ => println("confused....")
  }

  // implicits
  implicit val timeout = 3000
  def setTimeout(f : () => Unit)(implicit defaultTimeout : Int) = f()

  setTimeout(() => println(("timeout"))) //defaultTimeout 은 없어도 됨.

  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(string: String) : Person = Person(string)

  println("Peter".greet)

  //implicit class
  implicit class Dog(name: String) {
    def bark = println("bark")
  }

  "Lassie".bark

  implicit val inverseOrdering : Ordering[Int] = Ordering.fromLessThan(_ > _)

  List(1,2,3).sorted
  println(List(1,2,3).sorted)

  // implicit scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello, future")
  }

  object Person {
    implicit val personOrdering : Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  println(List(Person("Bob"), Person("Alice")).sorted)
}
