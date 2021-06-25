package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object DirectivesBreakdown extends App {
  implicit val system = ActorSystem("LowLevelServerAPI")

  import system.dispatcher

  // directives
  import akka.http.scaladsl.server.Directives._

  // Filtering directives

  val simpleHttpMethods =
    post { // get, put, patch, delete, head, options
      complete(StatusCodes.Forbidden)
    }

  val simplePathRoute =
    path("about") {
      complete(StatusCodes.OK)
    }

  val completePathRoute =
    path("api" / "myEndpoint") { // accessible at api/myEndpoint
      complete(StatusCodes.OK)
    }

  val dontConfuse =
    path("api/myEndpoint") { // accessible at api%fmyEndpoint
      complete(StatusCodes.OK)
    }

  val pathEndRoute =
    pathEndOrSingleSlash { // localhost:8080 OR localhost:8080/
      complete(StatusCodes.OK)
    }

  // Http().newServerAt("localhost", 8080).bind(dontConfuse)

  // extraction directives
  val pathExtractionRoute =
    path("api" / "item" / IntNumber) { itemNumber => // in case of string we are going to receive "The requested resource could not be found."
      println(s"I've got a number in my path: $itemNumber")
      complete(StatusCodes.OK)
    }

  // /api/item?id=45
  val queryParamExtractionRoute =
    path("api" / "item") {
      parameter("id") { (itemId: String) =>
        println(s"I've extracted from query params: $itemId")
        complete(StatusCodes.OK)
      }
    }

  val extractRequestRoute =
    path("endpoint") {
      extractRequest { httpRequest: HttpRequest =>
        extractLog { log =>
          log.info(s"I've got the http request: $httpRequest")
          complete(StatusCodes.OK)
        }
      }
    }

  val extractRequestCompactRoute = (path("endpoint") & extractRequest & extractLog) { (request, log) =>
    log.info(s"I've got the http request: $request")
    complete(StatusCodes.OK)
  }

  val dryRoute =
    (path("about") | path("aboutUs")) {
      complete(StatusCodes.OK)
    }

  // yourblog.com/42 and yourblog.com?postId=42
  val combinedRoute =
    (path(IntNumber) | parameter((Symbol("postId").as[Int]))) { (blogId: Int) =>
      complete(StatusCodes.OK)
    }

  // Http().newServerAt("localhost", 8080).bind(pathExtractionRoute)

  // Actionable directives


}
