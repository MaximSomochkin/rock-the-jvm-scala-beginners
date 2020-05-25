package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  object FussyKid {

    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {

    import FussyKid._
    import Mom._

    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject

    }
  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive) //change my receive handler to sadReceive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => //stay sad
      case Food(CHOCOLATE) => context.become(happyReceive) // sender() ! KidAccept//change my receive handler to sadReceive
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {

    case class MomStart(kidRef: ActorRef)

    case class Food(food: String)

    case class Ask(message: String) //do you want to play
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {

    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        //test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play?")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("Yay, my kid is sad, but he is healthy!")
    }
  }

  val system = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  //mom ! MomStart(fussyKid)

  mom ! MomStart(statelessFussyKid)

  /*
  mom receives MomStart
    kid receives Food(veg) -> kid will change the handler to sadReceive
    kid receives Ask(play?) -> kid replies with the sadReceive handler
  mom receives kidReject
  * */

  /**
   * Exersicise 1 - recreate the CounterActor with context.become and NO MUTABLE STATE
   **/
  object CounterActor {

    case object Increment

    case object Decrement

    case object Print

  }

  class CounterActor extends Actor {

    import CounterActor._

    var counter = 0


    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[countReceive($currentCount)] incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"[countReceive($currentCount)] decrementing")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my current count is $currentCount")
    }


  }

  import CounterActor._

  val counterActor = ActorSystem("counterActor").actorOf(Props[CounterActor], "counterActor")
  //counterActor ! "decrement"
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  /**
   * Exersize 2  - a simplified voting system
   **/
/************************************************MUTABLE VARIANT**********************/
//  case class Vote(candidate: String)
//
//  case object VoteStatusRequest
//
//  case class VoteStatusReply(candidate: Option[String])
//
//  class Citizen extends Actor {
//    var candidate: Option[String] = None
//
//    override def receive: Receive = {
//      case Vote(c) => context.become(voted(Some(c)))//candidate = Some(c)
//      case VoteStatusRequest => sender() ! VoteStatusReply(None/*candidate*/)
//    }
//    def voted(candidate: Some[String]): Receive = {
//      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
//    }
//  }
//
//  case class AgregateVotes(citizens: Set[ActorRef])
//
//  class VoteAgregator extends Actor {
//    var stillWaiting: Set[ActorRef] = Set()
//    var currentStatus: Map[String, Int] = Map()
//
//    override def receive: Receive = {
//      case AgregateVotes(citizens) =>
//        stillWaiting = citizens
//        citizens.foreach(citizenRef=>citizenRef ! VoteStatusRequest)
//      case VoteStatusReply(None) =>
//        //a citizen hasn't voted yet
//        sender() ! VoteStatusRequest //this might end up in an infinite loop
//      case VoteStatusReply(Some(candidate)) =>
//        val newStillWaiting = stillWaiting - sender()
//        val currentVotesOfCandidate = currentStatus.getOrElse(candidate,0)
//        currentStatus = currentStatus + (candidate -> (currentVotesOfCandidate +1))
//        if (newStillWaiting.isEmpty){
//          println(s"[agregator] poll status: $currentStatus")
//        }else{
//          stillWaiting = newStillWaiting
//        }
//    }
//  }
/***************************************************************************************************************/


  /**********************************************IMMUTABLE VARIANT**********************************************/

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {


    override def receive: Receive = {
      case Vote(c) => context.become(voted (c))//candidate = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(None/*candidate*/)
    }
    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AgregateVotes(citizens: Set[ActorRef])

  class VoteAgregator extends Actor {
       override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive ={
      case AgregateVotes(citizens) =>
          citizens.foreach(citizenRef=>citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }
    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStatus: Map[String, Int]): Receive ={
      case VoteStatusReply(None) =>
        //a citizen hasn't voted yet
        sender() ! VoteStatusRequest //this might end up in an infinite loop
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStatus.getOrElse(candidate,0)
        val newStatus = currentStatus + (candidate -> (currentVotesOfCandidate +1))
        if (newStillWaiting.isEmpty){
          println(s"[agregator] poll status: $newStatus")
        }else{
          //still need to process some status
          context.become(awaitingStatuses(newStillWaiting, newStatus))
        }
    }
  }

    val alice = system.actorOf(Props[Citizen])
    val bob = system.actorOf(Props[Citizen])
    val charlie = system.actorOf(Props[Citizen])
    val daniel = system.actorOf(Props[Citizen])

    alice ! Vote("Martin")
    bob ! Vote("Jones")
    charlie ! Vote("Roland")
    daniel ! Vote("Roland")

  val voteAgregator = system.actorOf(Props[VoteAgregator])
  voteAgregator ! AgregateVotes(Set(alice, bob, charlie, daniel))

  /*
  Prrint the status of the votes
  * */
}
