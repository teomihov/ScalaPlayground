package rockthejvm.akkahttp.part2_lowlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem}
import spray.json._

case class Guitar(make: String, model: String)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars
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
      guitars = guitars  + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1
  }
}

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat = jsonFormat2(Guitar)
}

object RestfulWithJson extends App with GuitarStoreJsonProtocol {
  implicit val system = ActorSystem("LowLevelRest")
  import system.dispatcher

  /*
    Get on localhost: 8080/api/guitar => All the guitars in the store
    Post on localhost: 8080/api/guitar => insert the guitar into the store
   */

  val simpleGuitar = Guitar("Fender", "Stratocaster")
  println(simpleGuitar.toJson.prettyPrint)
}
