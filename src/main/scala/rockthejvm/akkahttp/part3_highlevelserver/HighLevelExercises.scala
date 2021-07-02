package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import rockthejvm.akkahttp.part2_lowlevelserver.GuitarDB.CreateGuitar
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Failure}

trait PeopleJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat = jsonFormat2(Person)
}

case class Person(pin: Int, name: String)

object HighLevelExercises extends App with PeopleJsonProtocol {
  implicit val system = ActorSystem("HighLevelExercises")

  import system.dispatcher

  /**
   * Exercise:
   * - Get /api/people - retrieve all the people you have registered
   * - Get /api/people/pin - retrieve the person with that PIN returns as JSON
   * - Get /api/people?pin=X (same)
   * - (harder) POST /api/people with a JSON payload denoting a Person, add that person to your db
   *
   */

  var people = List(
    Person(1, "Alice"),
    Person(2, "Bob"),
    Person(3, "Charlie"),
  )

  def toHttpEntity(payload: String) = HttpEntity(ContentTypes.`application/json`, payload)

  val route =
    pathPrefix("api" / "people") {
      get {
        (path(IntNumber) | parameter("pin".as[Int])) { pin =>
          complete(toHttpEntity(people.find(_.pin == pin).toJson.prettyPrint))
        }
        pathEnd {
          complete(toHttpEntity(people.toJson.prettyPrint))
        }
      }
      post {
        (pathEnd & extractRequest & extractLog) { (request, log) =>
          val entityStrictFuture = request.entity.toStrict(3.seconds)
          val futurePerson = entityStrictFuture.map(esf => esf.data.utf8String.parseJson.convertTo[Person])

          // instead of this code we can use directive onComplete
          //          futurePerson onComplete {
          //            case Success(person) =>
          //              people = people :+ person
          //              log.info(s"Successfully added person: $person")
          //            case Failure(e) =>
          //              log.info(s"Error: $e")
          //          }
          //
          //          complete(futurePerson.map(_ => StatusCodes.OK).recover {
          //            case _ => StatusCodes.InternalServerError
          //          })

          onComplete(futurePerson) {
            case Success(person) =>
              log.info(s"Successfully added person: $person")
              people = people :+ person
              complete(StatusCodes.OK)
            case Failure(e) =>
              log.info(s"Error: $e")
              failWith(e)
          }
        }
      }
    }

  Http().newServerAt("localhost", 8080).bind(route)
}
