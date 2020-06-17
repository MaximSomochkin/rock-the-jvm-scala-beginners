package part6patterns

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class FSMSpec extends TestKit(ActorSystem("FSMSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import FSMSpec._

  "A vending machine" should {
    "error when not initialized" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! RequestProduct("coke")
      expectMsg(VendingError("MachineNotInitialized"))
    }

    "report product not available" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 1))
      vendingMachine ! RequestProduct("sandwich")
      expectMsg(VendingError("ProductNotAvailable"))

    }

    "throw timeout if i don't insert money" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 1))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 1 dollars"))

      within(1.5 second) {
        expectMsg(VendingError("RequestTimedOut"))
      }

    }

    "handle the reception of partial money" in{
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(1)
      expectMsg(Instruction("Please insert 2 dollars"))

      within(1.5 second) {
        expectMsg(VendingError("RequestTimedOut"))
        expectMsg(GiveBackChange(1))
      }
    }

    "deliver the product if I insert all the money" in{
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(3)
      expectMsg(Deliver("coke"))
    }

    "give back change and be able to request money to a new product" in{
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(5)
      expectMsg(Deliver("coke"))
      expectMsg(GiveBackChange(2))
    }
  }

}

object FSMSpec {

  // vending machine

  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])

  case class RequestProduct(product: String)

  case class Instruction(instruction: String)

  case class ReceiveMoney(amount: Int)

  case class Deliver(product: String)

  case class GiveBackChange(amount: Int)

  case class VendingError(reason: String)

  case object ReceiveMoneyTimeout

  class VendingMachine extends Actor with ActorLogging {
    implicit val executionContext: ExecutionContext = context.dispatcher

    override def receive: Receive = idle

    def idle: Receive = {
      case Initialize(inventory, prices) => context.become(operational(inventory, prices))
      case _ => sender() ! VendingError("MachineNotInitialized")
    }

    def operational(inventory: Map[String, Int], prices: Map[String, Int]): Receive = {
      case RequestProduct(product) => inventory.get(product) match {
        case None | Some(0) =>
          sender() ! VendingError("ProductNotAvailable")
        case Some(_) =>
          val price = prices(product)
          sender() ! Instruction(s"Please insert $price dollars")
          context.become(waitForMoney(inventory, prices, product, 0, startReceiveMoneyTimeoutSchedule, sender()))
      }
    }

    def waitForMoney(inventory: Map[String, Int],
                     prices: Map[String, Int],
                     product: String,
                     money: Int,
                     moneyTimeoutShedule: Cancellable,
                     requester: ActorRef
                    ): Receive = {
      case ReceiveMoneyTimeout =>
        requester ! VendingError("RequestTimedOut")
        if (money > 0) requester ! GiveBackChange(money)
        context.become(operational(inventory, prices))
      case ReceiveMoney(amount) =>
        moneyTimeoutShedule.cancel()
        val price = prices(product)

        if (money + amount >= price) {
          //user buys product
          requester ! Deliver(product)
          //deliver the change
          if (money + amount - price > 0) requester ! GiveBackChange(money + amount - price)
          //updating inventory
          val newStock = inventory(product) - 1
          val newInventory = inventory + (product -> newStock)
          context.become(operational(newInventory, prices))
        } else {
          val remainingMoney = price - money - amount
          requester ! Instruction(s"Please insert $remainingMoney dollars")
          context.become(waitForMoney(inventory, prices, product, //do not change
            money + amount, // user has insert some money
            startReceiveMoneyTimeoutSchedule, // i need to set the timeout again
            requester))
        }
    }

    def startReceiveMoneyTimeoutSchedule = context.system.scheduler.scheduleOnce(1 second) {
      self ! ReceiveMoneyTimeout
    }

  }

}


