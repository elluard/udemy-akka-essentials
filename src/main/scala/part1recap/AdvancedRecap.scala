package part1recap

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
}
