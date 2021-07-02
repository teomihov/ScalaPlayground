package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
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

object WebSocketsDemo extends App {
  implicit val system = ActorSystem("WebSocketsDemo")
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val textMessage = TextMessage(Source.single("Hello via a text message"))
  val binaryMessage = BinaryMessage(Source.single(CompactByteString("Hello via a binary message")))

  val html =
    """
      |<html>
      |    <head>
      |        <script>
      |            var exampleSocket = new WebSocket("ws://localhost:8080/greeter");
      |            console.log("starting websocket...");
      |
      |            exampleSocket.onmessage = function(event) {
      |                var newChild = document.createElement("div");
      |                newChild.innerText = event.data;
      |                document.getElementById("1").appendChild(newChild);
      |            };
      |
      |            exampleSocket.onopen = function(event) {
      |                exampleSocket.send("socket seems to be open...");
      |            };
      |
      |            exampleSocket.send("socket says: hello, server!");
      |        </script>
      |    </head>
      |
      |    <body>
      |        Starting websocket...
      |        <div id="1">
      |        </div>
      |    </body>
      |
      |</html>
    """.stripMargin


  def webSocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
    case tm: TextMessage =>
      TextMessage(Source.single("Server says back") ++ tm.textStream)
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      TextMessage(Source.single("Server received a binary message "))
  }
  val webSocketsRoute =
    (pathEndOrSingleSlash & get) {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          html
        )
      )
    } ~
    path("greeter") {
      handleWebSocketMessages(socialFlow)
    }

  Http().newServerAt("localhost", 8080).bind(webSocketsRoute)

  case class SocialPost(owner: String, content: String)

  val socialFeed = Source(
    List(
      SocialPost("Martin", "no no no"),
      SocialPost("Teo", "yoyoyo"),
      SocialPost("Teo", "me again"),
    )
  )

  val socialMessages = socialFeed.throttle(1, 2.seconds).map(socialPost => TextMessage(s"${socialPost.owner} said ${socialPost.content}"))
  val socialFlow: Flow[Message, Message, Any] = Flow.fromSinkAndSource(
    Sink.foreach[Message](println),
    socialMessages
  )


}
