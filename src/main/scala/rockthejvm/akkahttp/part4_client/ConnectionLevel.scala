package rockthejvm.akkahttp.part4_client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object ConnectionLevel extends App {

  implicit val system = ActorSystem("LowLevelServerAPI")

  import system.dispatcher

  // https://sales.bcpea.org/properties?court=2&city=563&type=&price=&construction=&floor=&judge=&region=6&area%5Bfrom%5D=&area%5Bto%5D=
  val connectionFlow = Http().outgoingConnection("sales.bcpea.org")

  def oneOffRequest(request:HttpRequest) =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  oneOffRequest(HttpRequest()).onComplete {
    case Success(res) => println(s"Success: $res")
    case Failure(ex) => println(s"Error: $ex")
  }

}
