package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

case class Player(nickname: String, characterClass: String, level: Int)

object GameAreaMap {
  case object GetAllPlayers

  case class GetPlayer(nickname: String)

  case class GetPlayerByClass(characterClass: String)

  case class AddPlayer(player: Player)

  case class RemovePlayer(player: Player)

  case object OperationSuccess
}

class GameAreaMap extends Actor with ActorLogging {

  import GameAreaMap._

  var players = Map[String, Player]()

  override def receive: Receive = {
    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList

    case GetPlayer(nickname) =>
      log.info(s"Get player with nickname $nickname")
      sender() ! players.get(nickname)

    case GetPlayerByClass(characterClass) =>
      log.info(s"Get players with the character class $characterClass")
      sender() ! players.values.toList.filter(_.characterClass == characterClass)

    case AddPlayer(player) =>
      log.info(s"Trying to add player $player")
      players = players + (player.nickname -> player)
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Trying to remove player $player")
      players = players - player.nickname
      sender() ! OperationSuccess
  }
}


trait PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit val playerFormat = jsonFormat3(Player)
}

object MarshallingJSON extends App with PlayerJsonProtocol with SprayJsonSupport {

  import GameAreaMap._

  implicit val system = ActorSystem("MarshallingJSON")

  import system.dispatcher

  val rtjvmGameMap = system.actorOf(Props[GameAreaMap], "gameAreaMap")
  val playersList = List(
    Player("martin_killz_u", "Warrior", 70),
    Player("rolandBrave", "Elf", 50),
    Player("daniel_rock03", "Wizard", 30),
  )

  playersList.foreach { p =>
    rtjvmGameMap ! AddPlayer(p)
  }

  /*
    - GET /api/player - all players
    - GET /api/player/nickname - returns the player with the given nickname
    - GET /api/player?nickname=x- returns the player
    - GET /api/player/class/(charClass)- returns all the player with the given character class
    - POST /api/player with json payload - add player to the map
    - DELETE /api/player with JSON payload, removes the player from the map
   */

  implicit val timeout = Timeout(2.seconds)
  val gameRoute = pathPrefix("api" / "player") {
    get {
      path("class" / Segment) { characterClass =>
        val playerFuture = (rtjvmGameMap ? GetPlayerByClass(characterClass)).mapTo[List[Player]]
        complete(playerFuture)
      } ~
      (path(Segment) | parameter("nickname")) { nickname =>
        val playerFuture = (rtjvmGameMap ? GetPlayer(nickname)).mapTo[Option[Player]]
        complete(playerFuture)
      } ~
      pathEndOrSingleSlash {
        complete((rtjvmGameMap ? GetAllPlayers).mapTo[List[Player]])
      }
    } ~
    post {
      entity(as[Player]) { player =>
        complete((rtjvmGameMap ? AddPlayer(player)).map(_ => StatusCodes.OK))
      }
    } ~
    delete {
      entity(as[Player]) { player =>
        complete((rtjvmGameMap ? RemovePlayer(player)).map(_ => StatusCodes.OK))
      }
    }

  }

  Http().newServerAt("localhost", 8080).bind(gameRoute)
}
