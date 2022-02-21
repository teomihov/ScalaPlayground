package akka.documentation

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object IntroductionToActors  extends App {
  object HelloWorld {
    final case class Greet(whom: String, replyTo: ActorRef[Greeted])
    final case class Greeted(whom: String, from: ActorRef[Greet])

    def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
      context.log.info("Hello {}!", message.whom)
      message.replyTo ! Greeted(message.whom, context.self)
      Behaviors.same
    }
  }
  object HelloWorldBot {
    def apply(max: Int): Behavior[HelloWorld.Greeted] = {
      bot(0, max)
    }

    private def bot(greetingCounter: Int, max: Int): Behavior[HelloWorld.Greeted] = {
      Behaviors.receive { (context, message) =>
        val n = greetingCounter + 1
        context.log.info("Greeting {} for {}", n, message.whom)
        if (n == max) {
          Behaviors.stopped
        } else {
          message.from ! HelloWorld.Greet(message.whom, context.self)
          bot(n, max)
        }
      }
    }
  }

  object HelloWorldMain {
    final case class SayHello(name: String)

    def apply(): Behavior[SayHello] = {
      Behaviors.setup { context =>
        val greeter = context.spawn(HelloWorld(), "greeter")

        Behaviors.receiveMessage { message =>
          val replyTo = context.spawn(HelloWorldBot(3), message.name)
          greeter ! HelloWorld.Greet(message.name, replyTo)
          Behaviors.same
        }
      }
    }
  }

  val system: ActorSystem[HelloWorldMain.SayHello] =
    ActorSystem(HelloWorldMain(), "hello")

  system ! HelloWorldMain.SayHello("World")
  //system ! HelloWorldMain.SayHello("Akka")
}

object MoreComplexExample extends App {

  object ChatRoom {
    sealed trait RoomCommand
    final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand
    final case class PublishSessionMessage(screenName: String, message: String) extends RoomCommand

    sealed trait SessionEvent
    final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
    final case class SessionDenied(reason: String) extends SessionEvent
    final case class MessagePosted(screenName: String, message: String) extends SessionEvent

    sealed trait SessionCommand
    final case class PostMessage(message: String) extends SessionCommand
    final case class NotifyClient(message: MessagePosted) extends SessionCommand

    def apply(): Behavior[RoomCommand] =
      chatRoom(List.empty)

    private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] =
      Behaviors.receive { (context, message) =>
        message match {
          case GetSession(screenName, client) =>
            // create a child actor for further interaction with the client
            context.log.info(s"Get Session with screenName: $screenName and client: $client ")
            val ses = context.spawn(
              session(context.self, screenName, client),
              name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
            client ! SessionGranted(ses)
            chatRoom(ses :: sessions)
          case PublishSessionMessage(screenName, message) =>
            context.log.info(s"PublishSessionMessage: $screenName and message: $message ")
            val notification = NotifyClient(MessagePosted(screenName, message))
            sessions.foreach(_ ! notification)
            Behaviors.same
        }
      }

    private def session(
                         room: ActorRef[PublishSessionMessage],
                         screenName: String,
                         client: ActorRef[SessionEvent]): Behavior[SessionCommand] =
      Behaviors.receiveMessage {
        case PostMessage(message) =>
          // from client, publish to others via the room
          println(s"Session PostMessage: $message")
          room ! PublishSessionMessage(screenName, message)
          Behaviors.same
        case NotifyClient(message) =>
          println(s"Session NotifyClient: $message")
          // published from the room
          client ! message
          Behaviors.same
      }
  }

  object Gabbler {
    import ChatRoom._

    def apply(): Behavior[SessionEvent] =
      Behaviors.setup { context =>
        Behaviors.receiveMessage {
          case SessionGranted(handle) =>
            context.log.info(s"Gabbler SessionGranted: $handle")
            handle ! PostMessage("Hello World!")
            Behaviors.same
          case MessagePosted(screenName, message) =>
            context.log.info("message has been posted by '{}': {}", screenName, message)
            Behaviors.stopped
        }
      }
  }

  object Main {
    def apply(): Behavior[NotUsed] =
      Behaviors.setup { context =>
        val chatRoom = context.spawn(ChatRoom(), "chatroom")
        val gabblerRef = context.spawn(Gabbler(), "gabbler")
        context.watch(gabblerRef)
        chatRoom ! ChatRoom.GetSession("ol’ Gabbler", gabblerRef)

        Behaviors.receiveSignal {
          case (_, Terminated(_)) =>
            Behaviors.stopped
        }
      }
  }

  ActorSystem(Main(), "ChatRoomDemo")
}
