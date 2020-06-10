package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

//step 1 - import the ask pattern
import akka.pattern.ask
import akka.pattern.pipe

class AskSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._

  "An autentificator" should {
    authenticatorTestSuite(Props[AuthManager])
  }

  "A piped autentificator" should {
    authenticatorTestSuite(Props[PipedAuthManager])
  }

  def authenticatorTestSuite(props: Props) = {
    import AuthManager._

    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(props)
      authManager ! Authentificate("max", "rtjvm")
      expectMsg(AuthFailure(AUTH_FAILURE_USER_NOT_FOUND))
    }

    "fail to authenticate if invalid password" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("max", "rtjvm")
      authManager ! Authentificate("max", "invalidPassword")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
    }
    "successfuly authenticate a registred user" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("max", "rtjvm")
      authManager ! Authentificate("max", "rtjvm")
      expectMsg(AuthSuccess)
    }
  }
}
object AskSpec {

  // this code is somewhere else in your App
  case class Read(key: String)

  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key) //Option[String]
      case Write(key, value) =>
        log.info(s"Writing the value $value for the $key")
        context.become(online(kv + (key -> value)))
    }
  }

  // user authentificator actor
  case class RegisterUser(username: String, password: String)

  case class Authentificate(username: String, password: String)

  case class AuthFailure(message: String)

  case object AuthSuccess

  object AuthManager {
    val AUTH_FAILURE_USER_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
    val AUTH_FAILURE_SYSTEM = "system error"

  }

  class AuthManager extends Actor with ActorLogging {

    import AuthManager._

    //step 2 - logistics
    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDB = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDB ! Write(username, password)
      case Authentificate(username, password) => handleAuthentication(username, password)

    }


    def handleAuthentication(username: String, password: String): Unit = {
      val originalSender = sender() // IMPORTANT step 5
      // step 3 - ask the actor
      val future = authDB ? Read(username)

      // step 4 - handle the future for e.g. with onComplete
      future.onComplete {
        // step 5 MOST IMPORTANT
        //NEVER CALL METHODS ON THE ACTOR INSTANCE OR ACCESS MUTABLE STATE IN ON COMPLETE
        // avoid closing over the actor instance or mutable state
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_USER_NOT_FOUND)
        case Success(Some(dbPassword)) =>
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }

  class PipedAuthManager extends AuthManager{
    import AuthManager._

    override def handleAuthentication(username: String, password: String): Unit = {
      //step 3 - ask the actor
      val future = authDB ? Read(username) //Future[Any]
     // step 4 -  process the future util you get the responses you will send back
      val passwordFuture = future.mapTo[Option[String]] // Future[Option[String]]
      val responseFuture = passwordFuture.map{
        case None =>AuthFailure(AUTH_FAILURE_USER_NOT_FOUND)
        case Some(dbPassword)=>
          if(dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
      }//Future[Any] - will be completed with the response I will send back

        //step 5 pipe the resulting future to the actor you want to send the result to

      //when the future completes, send the response to the actor ref in the arg list
      responseFuture.pipeTo(sender())
    }
  }

}