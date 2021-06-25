package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route

object HighLevelIntro extends App {
  implicit val system = ActorSystem("LowLevelServerAPI")

  import system.dispatcher

  // directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("home") { // directive
      get {
        complete(StatusCodes.OK) // directive
      }
    }

  Http().newServerAt("localhost", 8080).bind(simpleRoute)
}
