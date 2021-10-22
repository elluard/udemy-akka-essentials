package part1recap

object GeneralRecap extends App {
  val aCondition: Boolean = false

  var aVariable = 53
  aVariable += 1

  val aConditionedVal = if (aCondition) 42 else 65

  val aCodeBlock = {
    println("run")
    if(aCondition) 74
    56
  }

  val theUnit : Unit = println("Hello, Scala")

  def aFunction(x : Int): Int = x + 1

  def factorial(n: Int, acc: Int) : Int =
    if(n <= 0) acc
    else factorial(n - 1, acc * n)

  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a : Animal) : Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch!")
  }

  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  val aCanivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar")
  }

  aCanivore eat aDog

  abstract class MyList[+A]

  object MyList

  case class Person(name: String, age : Int)

  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent, I swear!")
  } catch {
    case e: Exception => "I caught an exception"
  } finally {
    print("some logs")
  }

  //functional programming
  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int) : Int = v1 + 1
  }

  val incremented = incrementer(42)
  // incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int === Function[Int, Int]


}
