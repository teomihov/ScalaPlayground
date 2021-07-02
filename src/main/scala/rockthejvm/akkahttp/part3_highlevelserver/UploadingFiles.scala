package rockthejvm.akkahttp.part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.javadsl.server.MissingQueryParamRejection
import akka.http.scaladsl._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, Rejection, RejectionHandler, Route}
import akka.pattern.ask
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.{CompactByteString, Timeout}
import rockthejvm.akkahttp.part2_lowlevelserver.GuitarDB.CreateGuitar
import rockthejvm.akkahttp.part3_highlevelserver.GameAreaMap.GetAllPlayers
import spray.json._

import java.io.File
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object UploadingFiles extends App {
  implicit val system = ActorSystem("WebSocketsDemo")
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val filesRoute = {
    (pathEndOrSingleSlash & get) {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <form action="http://localhost:8080/upload" method="post" enctype="multipart/form-data">
            |     <input type="file" name="myFile" />
            |     <button type="submit">Upload</button>
            |   </form>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    } ~
    (path("upload") & extractLog) { log =>
      entity(as[Multipart.FormData]) { formdata =>
        val partSource = formdata.parts
        val filePartsSink = Sink.foreach[Multipart.FormData.BodyPart] { bodyPart =>
          if(bodyPart.name == "myFile") {
            val filename = "src/main/resources/" + bodyPart.filename.getOrElse("tempFile_" + System.currentTimeMillis())
            val file = new File(filename)
            log.info(s"Writing to file: $filename")

            val fileContentsSource = bodyPart.entity.dataBytes
            val fileContentsSink = FileIO.toPath(file.toPath)
            fileContentsSource.runWith(fileContentsSink)

          }
        }
        val writeOperationFuture = partSource.runWith(filePartsSink)
        onComplete(writeOperationFuture) {
          case Success(_) => complete("File uploaded.")
          case Failure(ex) => complete(s"File failed to upload: $ex")
        }
      }
    }
  }

  Http().newServerAt("localhost", 8080).bind(filesRoute)
}
