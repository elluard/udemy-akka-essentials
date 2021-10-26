package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.system

import scala.sys.Prop

object Ch10Excercise extends App {
  /**
   * Excercises
   * 1. a Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   */

  val actorSystem = ActorSystem("Exercise")

  case class Increment(amount : Int)
  case class Decrement(amount : Int)
  class CounterActor extends Actor {
    var counter : Int = 0
    override def receive: Receive = {
      case Increment(amount) => counter += amount
      case Decrement(amount) => counter -= amount
      case "print" => println(s"[${self}] count $counter")
    }
  }

  val aCounterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")

  aCounterActor ! Increment(1)
  aCounterActor ! "print"
  aCounterActor ! Decrement(2)
  aCounterActor ! "print"

  case class Deposit(amount : Int)
  case class DepositOrder(amount : Int, bankAcct : ActorRef)
  case class Withdraw(amount: Int)
  case class WithdrawOrder(amount: Int, bankAcct : ActorRef)
  case class Success(cmd : String)
  case class Failure(cmd : String , msg: String)

  class BankAccount extends Actor {
    var balance : Int = 0

    override def receive: Receive = {
      case Deposit(amt) => {
        if (amt < 0) {
          sender() ! Failure("Deposit", "Amount cannot be under 0")
        }
        else {
          balance += amt
          println(s"deposit completed, balance - ${balance}")
          sender() ! Success("Deposit")
        }
      }
      case Withdraw(amt) if balance < amt => {
        println(s"withdraw, balance - ${balance}")
        sender() ! Failure("Withdraw","Not enough balance")
      }
      case Withdraw(amt) => {
        balance -= amt
        println(s"withdraw completed, balance - ${balance}")
        sender() ! Success("Withdraw")
      }
    }
  }

  class Customer extends Actor {
    override def receive: Receive = {
      case DepositOrder(amt, bankAcct) => bankAcct ! Deposit(amt)
      case WithdrawOrder(amt, bankAcct) => bankAcct ! Withdraw(amt)
      case Success(cmd) => println(s"${cmd} success")
      case Failure(cmd, msg) => println(s"${cmd} Failure - ${msg}")
    }
  }

  val bankAccount = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val customer = actorSystem.actorOf(Props[Customer], "customer")

  customer ! DepositOrder(-100, bankAccount)
  customer ! DepositOrder(100, bankAccount)
  customer ! WithdrawOrder(10, bankAccount)
  customer ! WithdrawOrder(100, bankAccount)
}
