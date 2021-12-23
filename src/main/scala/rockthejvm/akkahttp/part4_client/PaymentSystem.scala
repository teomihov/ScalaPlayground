package rockthejvm.akkahttp.part4_client

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, Rejection, RejectionHandler, Route}
import akka.pattern.ask
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.{CompactByteString, Timeout}
import rockthejvm.akkahttp.part2_lowlevelserver.GuitarDB.CreateGuitar
import rockthejvm.akkahttp.part3_highlevelserver.GameAreaMap.GetAllPlayers
import rockthejvm.akkahttp.part4_client.PaymentSystemDomain.{PaymentAccepted, PaymentRejected, PaymentRequest}
import spray.json._

import java.io.File
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

case class CreditCard(serialNumber: String, securityCode: String, account: String)

object PaymentSystemDomain {
  case class PaymentRequest(creditCard: CreditCard, receiverAccount: String, amount: Double)

  case object PaymentRejected

  case object PaymentAccepted
}

class PaymentValidator extends Actor with ActorLogging {

  import PaymentSystemDomain._

  override def receive: Receive = {
    case PaymentRequest(CreditCard(serialNumber, _, senderAccount), receiverAccount, amount) =>
      log.info(s"$senderAccount is trying to send $amount dollars to $receiverAccount")
      if (serialNumber == "1234-1234-1234-1234") sender() ! PaymentRejected
      else sender() ! PaymentAccepted
  }
}

trait PaymentJsonProtocol extends DefaultJsonProtocol {
  implicit val creditCardFormat = jsonFormat3(CreditCard)
  implicit val paymentRequestFormat = jsonFormat3(PaymentSystemDomain.PaymentRequest)
}

object PaymentSystem extends App with PaymentJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem("LowLevelServerAPI")

  import system.dispatcher

  def test() : Future[Seq[String]] = Future {
    Seq("1", "2", "3")
  }

  test().map(_.foreach(el => {
    println("test")
    throw new RuntimeException()
  })).onComplete {
    case Success(_) =>
      println("organization-level aggregate maintenance jobs scheduled")
    case Failure(ex) =>
      println("exception while scheduling organization-level aggregate maintenance jobs")
  }

  test().map(_.foreach(el => {
    println("test")
    throw new RuntimeException()
  })).onComplete(x => println("V igrata sme: " + x))

    val paymentValidator = system.actorOf(Props[PaymentValidator], "paymentValidator")
  implicit val timeout = Timeout(2.seconds)
  val paymentRoute =
    path("api" / "payments") {
      post {
        entity(as[PaymentRequest]) { pr =>
          val validationResponseFuture = (paymentValidator ? pr).map {
            case PaymentRejected => StatusCodes.Forbidden
            case PaymentAccepted => StatusCodes.OK
            case _ => StatusCodes.NotFound
          }

          complete(validationResponseFuture)
        }
      }
    }

  Http().newServerAt("localhost", 8080).bind(paymentRoute)
}
