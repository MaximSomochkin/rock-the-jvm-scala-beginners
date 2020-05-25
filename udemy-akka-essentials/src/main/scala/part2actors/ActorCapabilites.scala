package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilites.BankAccount.{Deposit, Statement, TransactionFailure, TransactionSuccess, Withdraw}
import part2actors.ActorCapabilites.CounterActor.{Decrement, Increment, Print}
import part2actors.ActorCapabilites.Person.LiveTheLife
import part2actors.ActorsIntro.Person
//import part2actors.ActorCapabilites.CounterActor.{Decrement, Increment, Print}

object ActorCapabilites extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there!" //replying to message
      case message: String => println(s"[${context.self}] I have recived $message")
      case number: Int => println(s"[simple actor] I have recived a NUMBER: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have recived a something scecial: $contents")
      case SendMessageToYourself(content) =>
        simpleActor ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender of the WPM
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")


  // 1.message can be any Type
  // a)messages must be IMMUTABLE
  // b)messages must be SERIALIZABLE
  // in practice use case classes and case objects
  simpleActor ! "hello, actor"
  simpleActor ! 42 //who is the sender

  case class SpecialMessage(contents: String)

  simpleActor ! SpecialMessage("some special content")

  // 2. Actors have information about their context and about themselves
  // context.self === 'this' in OOP

  case class SendMessageToYourself(content: String)

  simpleActor ! SendMessageToYourself("I am actor, and i am proud of it")

  // actor can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4. Dead letters
  alice ! "Hi!"

  // 5. Forwarding messages
  // I'm ->A->B
  // forwarding = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob) // noSender

  /**
   * Exercises
   *
   * 1. A Counter actor
   * -increment
   * -decrement
   * -print
   *
   *  2. a Bank account sa an actor
   * receives
   * -Deposit an ammount
   * -Withdraw an amount
   * Statement
   * replies with
   * -Succes
   * -failure
   *
   * interact with some other kind of actor
   *
   **/

  //1
  /** ***************************************************************************/
  object CounterActor {

    case object Increment

    case object Decrement

    case object Print

  }

  class CounterActor extends Actor {

    import CounterActor._

    var counter = 0

    override def receive: Receive = {
      case Increment => counter += 1
       // println(s"counter inc and is $counter")
      case Decrement => counter -= 1
       // println(s"counter dec and is $counter")
      case Print => println(s"[counter] My current count is $counter")
    }
  }

  val counterActor = ActorSystem("counterActor").actorOf(Props[CounterActor], "counterActor")
  //counterActor ! "decrement"
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  // 2
  /** ***********************************************************************************/

  object BankAccount {

    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object Statement

    case class TransactionSuccess(message: String)

    case class TransactionFailure(reason: String)

  }

  class BankAccount extends Actor {

    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid deposit amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"Succesfully deposed amount $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid withdraw amount")
        else if (amount > funds) sender() ! TransactionFailure("unsufficients funds")
        else {
          funds -= amount
          sender() ! TransactionSuccess(s"Successfully withdrew $amount")
        }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }
    object Person {

      case class LiveTheLife(account: ActorRef)

    }

    class Person extends Actor {

      import Person._

      override def receive: Receive = {
        case LiveTheLife(account) =>
          account ! Deposit(10000)
          account ! Withdraw(90000)
          account ! Withdraw(500)
          account ! Statement
        case message => println(message.toString)
      }
    }



  val account = system.actorOf(Props[BankAccount],"bankAccount")
  val person = system.actorOf(Props[Person],"billionaire")

  person ! LiveTheLife (account)

}
