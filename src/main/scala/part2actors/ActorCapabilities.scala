package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!" //replying to a message
      case message: String => println(s"[${self}] I have received $message")
      case number: Int => println(s"[${context.self}] I have received a NUMBER $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward  (content + "s") // I keep the origineal sender of the message
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  simpleActor ! "hello actor"

  // 1 - messages can be of any type
  // a) message must be IMMUTABLE
  // b) message must be SERIALIZABLE

  // in practice use case classes and cas objects
  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self == this on OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob  = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref : ActorRef)
  alice ! SayHiTo(bob)

  alice ! "Hi" // reply to "me"

  // 5 - forwarding messages
  // Daniel -> Alice -> Bob

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)
}
