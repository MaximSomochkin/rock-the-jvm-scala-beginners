package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo  extends  App{
  class SimpleActorWithExplicitLogger extends  Actor{
// #1 explicit logging
    val logger = Logging(context.system, this)
    override def receive: Receive = {
            /*
            * 1 - DEBUG
            * 2 - INFO
            * 3 - WARNING
            * 4 - ERROR
            *
            * */
      case message => logger.info(message.toString) //LOG it
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"

  //#2 - ActorLogging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
          case(a,b) => log.info("Two things: {} and {}", a,b)
      case message => log.info(message.toString)
    }
  }

  val simpleActor = system.actorOf(Props[ActorWithLogging])
  simpleActor ! "Logging a simple by extending a trait"
  simpleActor !(42, 65)

}
