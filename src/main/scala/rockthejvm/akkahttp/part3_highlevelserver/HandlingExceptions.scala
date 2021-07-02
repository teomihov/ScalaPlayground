package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, Rejection, RejectionHandler, Route}
import akka.pattern.ask
import akka.util.Timeout
import rockthejvm.akkahttp.part2_lowlevelserver.GuitarDB.CreateGuitar
import rockthejvm.akkahttp.part3_highlevelserver.GameAreaMap.GetAllPlayers
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object HandlingExceptions extends App {
  implicit val system = ActorSystem("MarshallingJSON")
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val simpleRoute =
    path("api" / "people") {
      get {
        throw new RuntimeException("Getting all the people not working.")
      } ~
      post {
        parameter("id") { id =>
          if(id .length > 2)
            throw new NoSuchElementException("Cannot be found")

          complete(StatusCodes.OK)
        }
      }
    }

  implicit val customExceptionHandler = ExceptionHandler {
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, e.getMessage)
  }

  Http().newServerAt("localhost", 8080).bind(simpleRoute)

  val runtimeExceptionHandler = ExceptionHandler {
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
  }

  val delicateHandle =
    handleExceptions(runtimeExceptionHandler) { // handle all runtime exceptions
      path("api" / "people") {
        get {
          throw new RuntimeException("Getting all the people not working.")
        } ~
          post {
            parameter("id") { id =>
              if(id .length > 2)
                throw new NoSuchElementException("Cannot be found")

              complete(StatusCodes.OK)
            }
          }
      }
    }
}
