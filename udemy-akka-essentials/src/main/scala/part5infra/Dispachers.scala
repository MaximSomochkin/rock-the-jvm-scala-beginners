package part5infra

import scala.util.Random
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}


object Dispachers extends App {

  case object Message

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")

    }
  }

  val system = ActorSystem("DispatchersDemo" /*, ConfigFactory.load().getConfig("my-dispatcher")*/)

  //val simpleCounterActor = system.actorOf(Props[Counter].withDispatcher("my-dispatcher"))


  // method #1 - programmatic/in code
  val actors: Seq[ActorRef] = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  //  val r = new Random()
  //  for (i <- 1 to 1000){
  //    actors(r.nextInt(10))  ! i
  // }

  // method #2 - from config
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   * Dispachers implement the ExecutionContext trait
   **/

  class DBActor extends Actor with ActorLogging {
   // implicit val executionContext: ExecutionContext = context.dispatcher

    // solution #1 to not block important messages
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")

    // solution #2 to not block important messages use Router



    override def receive: Receive = {
      case message => Future {
        //wait on resource

        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }

  val dbActor = system.actorOf(Props[DBActor])
  dbActor ! "the meaning of the life is 42"

  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val message = s"important message $i"
    dbActor ! message
    nonBlockingActor ! message
  }
}
