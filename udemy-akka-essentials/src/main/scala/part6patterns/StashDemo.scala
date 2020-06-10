package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /**
   * ResourceActor
   *  - open => it can receive read/write requests to the resource
   *  - otherwise it will postpone all read/write requests until the state is open
   *
   * ResourceActor  is closed
   *    - Open => switch to the open state
   *    - Read, Write messages are POSTPONED
   *
   * ResourceActor is open
   *    - Read, Write are handled
   *    - Close => switch to the closed state
   *
   * [Open, Read, Read, Write]
   *      - switch to the open state
   *      - read the data
   *      - read the data again
   *      - write the data
   *
   * [Read, Open, Write]
   *      - stash Read
   * stash: [Read]
   *      - Open => switch to the open state
   * Mailbox: [Read, Write]
   *      - read and write are handled
   *
   **/

  case object Open

  case object Close

  case object Read

  case class Write(data: String)

  // mix-in Stash trait
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        // step 3 - unstashAll when you switch the message handler
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the closed state")
        // Step 2 - stash away what you can,t handle
        stash()
    }

    def open: Receive = {
      case Read =>
        // do some actual computation
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I am writing $data")
        innerData = data
      case Close =>
        log.info("Closing resource")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the open state")
        stash()
    }
  }

  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])

  resourceActor ! Read // stashed
  resourceActor ! Open // switch to the open; I have read ""
  resourceActor ! Open // stashed
  resourceActor ! Write("Hello stash")
  resourceActor ! Close // switch to closed; because stash contained Open message and in
                        // case Close contained unstashAll method, it switch to open contxt

  resourceActor ! Read //I have read
}
 /***BE CAREFUL ABOUT
  *     * potential memory bounds on stash
  *     * potential mailbox bounds when unstashing
  *     * no stashing twice
  *     * the Stash trait overrides preRestart so must be mixed-in last
  *
  * */