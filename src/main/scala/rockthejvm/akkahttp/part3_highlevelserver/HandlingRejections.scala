package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{ ActorSystem }
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{MethodRejection, Rejection, RejectionHandler, Route}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object HandlingRejections extends App {
  implicit val system = ActorSystem("MarshallingJSON")
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val simpleRoute =
    path("api" / "myEndpoint") {
      get {
        complete(StatusCodes.OK)
      } ~
      parameter("id") { _ =>
        complete(StatusCodes.OK)
      }
    }

  val badRequestHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I have encountered rejections: $rejections")
    Some(complete(StatusCodes.BadRequest))
  }

  val forbiddenHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I have encountered rejections: $rejections")
    Some(complete(StatusCodes.Forbidden))
  }


  val simpleRouteWithHandlers =
    handleRejections(badRequestHandler) {
      path("api" / "myEndpoint") {
        get {
          complete(StatusCodes.OK)
        } ~
        post {
          handleRejections(forbiddenHandler) {
            parameter("myParam") {_ =>
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }

  implicit val customRejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case m: MissingQueryParamRejection =>
        println(s"I got a query param rejection: $m")
        complete("Rejection Query Param")
    }
    .handle {
      case m: MethodRejection =>
        println(s"I got a method rejection: $m")
        complete("Rejection Method!")
    }
    .result()

  Http().newServerAt("localhost", 8080).bind(simpleRoute)
}
