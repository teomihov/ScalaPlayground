package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.util.Timeout
import rockthejvm.akkahttp.part2_lowlevelserver.{Guitar, GuitarDB, GuitarStoreJsonProtocol}
import spray.json._

import scala.concurrent.duration.DurationInt

object HighLevelExample extends App with GuitarStoreJsonProtocol {
  implicit val system = ActorSystem("LowLevelServerAPI")

  import system.dispatcher

  /*
    Setup
   */
  val guitarDb = system.actorOf(Props[GuitarDB], "LowLevelGuitarDB")
  val guitarList = List(
    Guitar("Fender", "Stratocaster"),
    Guitar("Gibson", "Les Paul", 1),
    Guitar("Martin", "LX1")
  )

  import GuitarDB._

  guitarList.foreach { guitar =>
    guitarDb ! CreateGuitar(guitar)
  }

  /*
    GET /api/guitar fetches all the guitars in the store
    GET /api/guitar?id=x fetches the guitar with id x
    GET /api/guitar/X fetrches guitar with id X
    GET /api/guitar/inventory
   */

  implicit val defaultTimeout = Timeout(2.seconds)

  val guitarServerRoute = {
    path("api" / "guitar") {
      get {
        complete {
          (guitarDb ? FindAllGuitars)
            .mapTo[List[Guitar]]
            .map { guitars =>
              HttpEntity(
                ContentTypes.`application/json`,
                guitars.toJson.prettyPrint
              )
            }
        }
      } ~
      get {
        parameter("id".as[Int]) { id =>
          complete {
            val guitarFuture = (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
            guitarFuture.map { guitar =>
              HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            }
          }
        }
      }
    } ~
    path("api" / "guitar"/ IntNumber) { id =>
      get {
        val guitarFuture = (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
        val result = guitarFuture.map { guitar =>
          HttpEntity(
            ContentTypes.`application/json`,
            guitar.toJson.prettyPrint
          )
        }

        complete(result)
      }
    } ~
    path("api" / "guitar" / "inventory") {
      get {
        parameter("inStock".as[Boolean]) { inStock =>
          val guitarFuture = (guitarDb ? FindGuitarsByQuantity(inStock)).mapTo[List[Guitar]]
          val result = guitarFuture.map { guitar =>
            HttpEntity(
              ContentTypes.`application/json`,
              guitar.toJson.prettyPrint
            )
          }

          complete(result)
        }
      }
    }
  }

  def toHttpEntity(payload: String) = HttpEntity(ContentTypes.`application/json`, payload)

  val simplifiedRoute =
    pathPrefix("api" / "guitar") {
      get {
        path("inventory") {
          parameter("inStock".as[Boolean]) { inStock =>
           complete {
             (guitarDb ? FindGuitarsByQuantity(inStock))
               .mapTo[List[Guitar]]
               .map(_.toJson.prettyPrint)
               .map(toHttpEntity)
           }
          }
        } ~
        (path(IntNumber) | parameter("id".as[Int])) { id =>
          complete {
            (guitarDb ? FindGuitar(id))
              .mapTo[Option[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          }
        } ~
        pathEndOrSingleSlash {
          complete {
            (guitarDb ? FindAllGuitars)
              .mapTo[List[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          }
        }
      }
    }

  Http().newServerAt("localhost", 8080).bind(guitarServerRoute)


}
