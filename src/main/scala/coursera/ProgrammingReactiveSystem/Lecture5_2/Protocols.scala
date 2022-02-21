package coursera.ProgrammingReactiveSystem.Lecture5_2

import akka.actor.Address
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import rockthejvm.akkaTyped.BaeldungAkkaTyped.PortfolioActor.Buy

import java.util.Date

object Protocols extends App {
  val greeter: Behavior[String] = {
    Behaviors.receiveMessage[String] { whom =>
      println(s"Hello $whom")
      Behaviors.stopped
    }
  }


  val system = ActorSystem(greeter, "greeter")
  system ! "boss"
  system ! "boss again" // throws an exception

  sealed trait Greeter
  final case class Greet(whom: String) extends Greeter
  final case object Stop extends Greeter

  val greeter2: Behavior[Greeter] = {
    Behaviors.receiveMessage[Greeter] { // this only receive messages, without context
      case Greet(whom) =>
        println(s"Hello $whom")
        Behaviors.same
      case Stop =>
        println("Shutting down")
        Behaviors.stopped
    }
  }

  // initialize and stay inactive after that
  ActorSystem[Nothing](Behaviors.setup[Nothing] { ctx =>
    val greeterRef = ctx.spawn(greeter2, "greeter")
    ctx.watch(greeterRef)

    greeterRef ! Greet ("world")
    greeterRef ! Stop

    Behaviors.empty
  }, "helloworld")

  sealed trait Guardian
  case class NewGreeter(replyTo: ActorRef[ActorRef[Greeter]]) extends Guardian
  case object Shutdown extends Guardian

  val guardian = Behaviors.receive[Guardian] { // receive a pair of the actor context and the message
    case (ctx, NewGreeter(replyTo)) =>
      val ref: ActorRef[Greeter] = ctx.spawnAnonymous(greeter2)
      replyTo ! ref
      Behaviors.same
    case (_, Shutdown) =>
      Behaviors.stopped
  }

  // buyer and supplier
  case class RequestQuote(title: String, buyer: ActorRef[Quote])
  case class Quote(price: BigDecimal, seller: ActorRef[BuyOrQuit])

  sealed trait BuyOrQuit
  case class Buy(address: Address, buyer: ActorRef[Shipping]) extends BuyOrQuit
  case object Quit extends BuyOrQuit

  case class Shipping(date: Date)

  sealed trait Secretary
  case class BuyBook(title: String, maxprice: BigDecimal, seller: ActorRef[RequestQuote]) extends Secretary
  case class QuoteWrapper(msg: Quote) extends Secretary
  case class ShippingWrapper(msg: Shipping) extends Secretary

  def secretary(address: Address) : Behavior[Secretary] =
    Behaviors.receivePartial {
      case (ctx, BuyBook(title, maxPrice, seller)) =>
        val quote = ctx.messageAdapter(QuoteWrapper)
        seller ! RequestQuote(title, quote)
        buyBook(maxPrice, address)
    }

  def buyBook(maxPrice: BigDecimal, address: Address) = ???
}
