package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random


class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._
  "A simple actor" should{
    "send back the same message" in {
      // testing scenario
      val echoActor = system.actorOf(Props[SimpleActor])
      val message ="Hello, test actor"
      echoActor ! message

      expectMsg(message)
      testActor
    }

  }
  "A BlackHole actor" should{
    "send back the same message" in {
      // testing scenario
      val blackHoleActor = system.actorOf(Props[BlackHoleActor])
      val message ="Hello, test actor"
      blackHoleActor ! message
      expectNoMessage(1.second)

      //expectMsg(message) //
    }
  }
  "A lab test actor" should{
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn the string in uppercase" in{
      labTestActor ! "I love akka"
      //expectMsg("I LOVE AKKA")

      val reply = expectMsgType[String]
      assert(reply=="I LOVE AKKA")
    }

    "reply to greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply to favorite Tech" in{
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in a different way" in{
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) //Seq[Any]

        // free to do more complicated assertions
    }

    "reply with cool tech in a factory way" in{
      labTestActor ! "favoriteTech"
      expectMsgPF(){
        case "Scala" =>
        case "Akka"=>
      }
    }
  }



}
object BasicSpec{
  class SimpleActor extends Actor{
    override def receive: Receive = {
      case message => sender() ! message
    }
  }
  class BlackHoleActor extends Actor{
    override def receive: Receive = {
      case_ =>
    }
  }

  class LabTestActor extends Actor{
    val random = new Random()

    override def receive: Receive = {
      case "greeting"=>
        if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}