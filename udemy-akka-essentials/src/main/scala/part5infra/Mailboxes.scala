package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {

  //val system = ActorSystem("MailboxDemo")
  val system = ActorSystem("MailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo")) // step 2

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * Case #1 - Custom priority mailbox
   * P0 -> most important
   * P1
   * P2
   * P3
   **/
  // stem #1 - mailbox difinition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config) //important paramrters
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[P0]") => 0
        case message: String if message.startsWith("[P0]") => 1
        case message: String if message.startsWith("[P0]") => 2
        case message: String if message.startsWith("[P0]") => 3
        case _ => 4
      })

  // step 2 - make it known in the config
  // step 3 - attach the dispatcher to an actor

  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  supportTicketLogger ! "[P3] this string would be nice to have"
  supportTicketLogger ! "[P0] this needs to be solve NOW!"
  supportTicketLogger ! "[P1] do this when you have time"

  // after which time can I send another message and be prioritized accordingly?

  /**
   * case #2 - control-aware mailbox
   * we'll use UnboundedControlAwareMailbox
   **/
  //step 1 - mark important messages sa control messages
  case object ManagmentTicket extends ControlMessage


  /*
  * step 2 - configure who gets the mailbox
  * -make the actor attach to the mailbox
  * */
  val controlAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
//  controlAwareActor ! "[P0] this needs to be solve NOW!"
//  controlAwareActor ! "[P1] do this when you have time"
//  controlAwareActor ! ManagmentTicket

  // method #2 - using deployment config
  val altControlAwareActor = system.actorOf(Props[SimpleActor], "altControlAwareActor")
  altControlAwareActor ! "[P0] this needs to be solve NOW!"
  altControlAwareActor ! "[P1] do this when you have time"
  altControlAwareActor ! ManagmentTicket
}
