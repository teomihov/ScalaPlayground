package rockthejvm.akkahttp.part3_highlevelserver
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, Rejection, RejectionHandler, Route}
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.{CompactByteString, Timeout}
import rockthejvm.akkahttp.part2_lowlevelserver.GuitarDB.CreateGuitar
import rockthejvm.akkahttp.part3_highlevelserver.GameAreaMap.GetAllPlayers
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}

object JWTAuthorization extends App with SprayJsonSupport {
  implicit val system = ActorSystem("JWTAuthorization")
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  import SecurityDomain._

  val superSecretPass = Map(
    "admin" -> "adminValue",
    "daniel" -> "RockThejvm"
  )

//  val loginRoute =
//    post {
//      entity(as[LoginRequest]) {
//        case LoginRequest(username, password) if checkPass(username, password) =>
//          val token = createToken(username, 1)
//          respondWithHeader(RawHeader("Access-Token", token)) {
//            complete(StatusCodes.OK)
//          }
//        case _ => complete(StatusCodes.Unauthorized)
//      }
//    }
}
