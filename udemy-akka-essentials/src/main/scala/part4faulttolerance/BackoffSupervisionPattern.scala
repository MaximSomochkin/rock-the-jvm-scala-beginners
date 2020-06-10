package part4faulttolerance

import java.io.File

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import scala.concurrent.duration._
import scala.io.Source

object BackoffSupervisionPattern extends App {

  case object ReadFile

  class FileBasedPresistentActor extends Actor with ActorLogging {
    var dataSource: Source = null

    override def preStart(): Unit = {
      log.info("Presistent actor starting")
    }

    override def postStop(): Unit = {
      log.warning("Presistent actor has stopped")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.warning("Presistent actor restarting")
    }

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
        //dataSource = Source.fromFile(new File("src/main/resources/testfiles/important.txt"))
        dataSource = Source.fromFile(new File("src/main/resources/testfiles/important.txt"))
        log.info("I've just read some important data: " + dataSource.getLines().toList)

    }
  }

  val system = ActorSystem("BackoffSupervisorDemo")
  //  val simpleActor = system.actorOf(Props[FileBasedPresistentActor], "simpleActor")
  //  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onFailure(
      Props[FileBasedPresistentActor],
      "simpleBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    )
  )

  //  val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
  //  simpleBackoffSupervisor ! ReadFile

  /**
   * simpleSupervisor
   * - child called simpleBackoffActor (props of type FileBasedPresistentActor)
   * - supervison strategy is the default one (restarting on everything)
   *  - first attempt after 3 seconds
   *  - next attempt is 2x the previous attempt
   **/

  val stopSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[FileBasedPresistentActor],
      "stopBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )

  //  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
  //  stopSupervisor ! ReadFile

  class EagerFBactor extends FileBasedPresistentActor {

  override def preStart(): Unit = {
    log.info("Eager actor starting")
    dataSource = Source.fromFile(new File("src/main/resources/testfiles/important.txt"))
  }
}
  val eagerActor = system.actorOf(Props[EagerFBactor])
    //ActorInitializationException => STOP

  val repeatedSupervisorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[FileBasedPresistentActor],
      "eagerActor",
      1 second,
      30 seconds,
      0.1
    )
  )
  val repeatedSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")
  repeatedSupervisor ! ReadFile

  /**
   * eagerSupervisor
   *  - child eagerActor
   *   - will die on start with ActorInitializationException
   *   - trigger the supervision strategy in  eagerSupervisor => STOP eagerActor
   *  - backoff will kick in after 1 second, 2s, 4, 8, 16
   * */
}
