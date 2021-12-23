package rockthejvm.akkaTyped

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

object AkkaTypedIncentives extends App {
  sealed trait ShoppingCartMessage
  case class AddItem(item: String) extends ShoppingCartMessage
  case class RemoveItem(item: String) extends ShoppingCartMessage
  case object ValidateCart extends ShoppingCartMessage

  val shoppingRootActor = ActorSystem (
    Behaviors.receiveMessage[ShoppingCartMessage] { message: ShoppingCartMessage =>
      message match {
        case AddItem(item) => println(s"Adding item $item.")
        case RemoveItem(item) => println(s"Removing item $item.")
        case ValidateCart => println("The card is good")
      }

      Behaviors.same
    },
    "simpleShoppingActor"
  )

   shoppingRootActor ! ValidateCart

  // Mutable State example

  val shoppingRootActor2 = ActorSystem (
    Behaviors.setup[ShoppingCartMessage] { ctx =>

      var items: Set[String] = Set()
        Behaviors.receiveMessage[ShoppingCartMessage] { message: ShoppingCartMessage =>
          message match {
            case AddItem(item) =>
              println(s"Adding item $item.")
              items += item
            case RemoveItem(item) =>
              println(s"Removing item $item.")
              items -= item
            case ValidateCart => println(s"The card is good. The items are: $items")
          }
          Behaviors.same
        }
      },
      "simpleShoppingActor"
  )

  def shoppingBehavior(items: Set[String]): Behavior[ShoppingCartMessage] =
    Behaviors.receiveMessage[ShoppingCartMessage] {
      case AddItem(item) =>
        println(s"Adding item $item.")
        shoppingBehavior(items + item)
      case RemoveItem(item) =>
        println(s"Removing item $item.")
        shoppingBehavior(items - item)
      case ValidateCart =>
        println(s"The card is good. The items are: $items")
        Behaviors.same
    }

  val rootOnlineStoreActor = ActorSystem(
    Behaviors.setup[ShoppingCartMessage] { ctx =>
      // create children HERE
      ctx.spawn(shoppingBehavior(Set()), "storeShoppingCart")

      Behaviors.same
    },
    "onlineStore"
  )

  // this should not work. There is no receive message behavior inside the ActorSystem(rootOnlineStoreActor)
  rootOnlineStoreActor ! AddItem("test")
}
