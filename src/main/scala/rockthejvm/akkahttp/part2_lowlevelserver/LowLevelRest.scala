package rockthejvm.akkahttp.part2_lowlevelserver

import akka.pattern.ask
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.model.Uri.{Query}
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

case class Guitar(make: String, model: String, quantity: Int = 0)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars

  case class AddQuantityToGuitar(id: Int, quantity: Int)
  case object AddedSuccessfully
  case class FindGuitarsByQuantity(hasQuantity: Boolean)
  case object NotFound
}

class GuitarDB extends Actor with ActorLogging {
  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars.")
      sender() ! guitars.values.toList
    case FindGuitar(id) =>
      log.info(s"Searching guitar by id: $id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar: Guitar) =>
      log.info(s"Adding guitar $guitar with id $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

    case AddQuantityToGuitar(id: Int, quantity: Int) =>
      // My way
//      log.info(s"Add quantity($quantity) to the guitar with id: $id")
//      val g = guitars.get(id)
//      g match {
//        case None => sender() ! NotFound
//        case Some(g) =>
//          self ! CreateGuitar(Guitar(g.make, g.model, quantity))
//          guitars = guitars.removed(id)
//          sender() ! AddedSuccessfully
//      }

      // Daniel's way
      val guitar = guitars.get(id)
      val newGuitar = guitar.map {
        case Guitar(model, make, q) => Guitar(model, make, quantity)
      }

      newGuitar.foreach(guitar => guitars = guitars + (id -> guitar))
      sender() ! AddedSuccessfully

    case FindGuitarsByQuantity(hasQuantity) =>
      log.info(s"Get all guitars that are in stock - $hasQuantity")
      if(hasQuantity) {
        sender() ! guitars.values.filter(x => x.quantity > 0).toList
      } else {
        sender() ! guitars.values.filter(x => x.quantity == 0).toList
      }
  }
}

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat = jsonFormat3(Guitar)
}

object RestfulWithJson extends App with GuitarStoreJsonProtocol {
  implicit val system = ActorSystem("LowLevelRest")
  import system.dispatcher

  val simpleGuitar = Guitar("Fender", "Stratocaster")
  println(simpleGuitar.toJson.prettyPrint)

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
    Server code
   */

  implicit val defaultTimeout = Timeout(2.seconds)

  def getGuitar(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    guitarId match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(id) =>
        val guitarFuture: Future[Option[Guitar]] = (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
        guitarFuture.map {
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(g) =>
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`application/json`,
                g.toJson.prettyPrint
              )
            )
        }
    }
  }

  def getAllInStockGuitars(query: Query) = {
    val hasQuantity = query.get("inStock").map(_.toBoolean)
    hasQuantity match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(hasQuantity) =>
        val guitarsFuture: Future[List[Guitar]] = (guitarDb ? FindGuitarsByQuantity(hasQuantity)).mapTo[List[Guitar]]
        guitarsFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
    }
  }

  def returnAllGuitars(): Future[HttpResponse] = {
    val guitarsFuture: Future[List[Guitar]] = (guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
    guitarsFuture.map { guitars =>
      HttpResponse(
        entity = HttpEntity(
          ContentTypes.`application/json`,
          guitars.toJson.prettyPrint
        )
      )
    }
  }

  def addQuantity(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    val quantityCount = query.get("quantity").map(_.toInt)
    (guitarId, quantityCount) match {
      case (Some(id), Some(quant)) =>
        val guitarFuture: Future[Any] = (guitarDb ? AddQuantityToGuitar(id, quant))
        guitarFuture.map {
          case NotFound => HttpResponse(StatusCodes.NotFound)
          case AddedSuccessfully =>
            HttpResponse(
              entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Added successfully.")
            )
        }
      case _ => Future(HttpResponse(StatusCodes.NotFound))
    }
  }

  val requestHandler: HttpRequest => Future[HttpResponse] = {
//    case HttpRequest(HttpMethods.GET, Uri.Path("/api/guitar"), _, _, _) =>
//      returnAllGuitars()

    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitar"), _, _, _) =>
      val query = uri.query()
      if(query.isEmpty) returnAllGuitars()
      else getGuitar(query)

    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitar"), _, entity, _) =>
      val entityStrictFuture = entity.toStrict(3.seconds)
      entityStrictFuture.flatMap { esf =>
        val guitarJsonString = esf.data.utf8String
        val guitar = guitarJsonString.parseJson.convertTo[Guitar]
        val guitarFuture = guitarDb ? CreateGuitar(guitar)
        guitarFuture.map { _ =>
          HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Successfully added the new guitar"))
        }
      }

    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitar/inventory"), _, _, _) =>
      val query = uri.query()
      if(query.isEmpty) {
        returnAllGuitars()
      } else {
        getAllInStockGuitars(query)
      }

    case HttpRequest(HttpMethods.POST, uri@Uri.Path("/api/guitar/inventory"), _, _, _) =>
      val query = uri.query()
      if(query.isEmpty) {
        returnAllGuitars()
      } else {
        addQuantity(query)
      }

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future {
        HttpResponse(status = StatusCodes.NotFound)
      }
  }

  Http().bindAndHandleAsync(requestHandler, "localhost", 8080)
}
