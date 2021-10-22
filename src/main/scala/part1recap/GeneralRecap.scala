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


}
