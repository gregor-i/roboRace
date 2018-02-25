package controller

import javax.inject.{Inject, Singleton}

import actor.GameStateActor
import actor.GameStateActor.GetState
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import gameLogic.GameState
import gameLogic.command.Command
import gameLogic.eventLog.EventLog
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.libs.streams.ActorFlow
import play.api.mvc.{InjectedController, WebSocket}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Singleton
class GameController @Inject()(implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext) extends InjectedController with Circe {
  val gameStateActor: ActorRef = system.actorOf(Props(classOf[GameStateActor], GameState.cycle0))

  implicit val timeout: Timeout = Timeout(5.seconds)

  def state() = Action.async {
    (gameStateActor ? GetState()).mapTo[GameState].map(state => Ok(state.asJson))
  }

  def view() = Action {
    Ok(views.html.RoboRally())
  }

  def events() = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => Props(new EventStreamActor(out, gameStateActor)))
  }
}


class EventStreamActor(out: ActorRef, gameStateActor: ActorRef) extends Actor {
  gameStateActor ! GameStateActor.Subscribe(self)

  private def sendToClients(event: EventLog): Unit = out ! event.asJson.noSpaces

  override def receive: Receive = {
    case events: Seq[EventLog] =>
      events.foreach(sendToClients)

    case event: EventLog =>
      sendToClients(event)

    case commandAsString: String =>
      parse(commandAsString).toOption.flatMap(_.as[Command].toOption) match {
        case Some(command) =>
          Logger.info(command.toString)
          gameStateActor ! command
        case None =>
          Logger.warn(s"'$commandAsString' could not be parsed as command")
      }
  }
}