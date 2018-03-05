package controller

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import akka.util.Timeout
import gameLogic.EventLog
import gameLogic.gameUpdate.Command
import gameLogic.processor.Processor
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.libs.streams.ActorFlow
import play.api.mvc.{InjectedController, WebSocket}
import repo.GameRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._
import play.filters.csrf.{CSRF, CSRFAddToken}

@Singleton
class GameController @Inject()(repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  implicit val timeout: Timeout = Timeout(5.seconds)

  private val id = "default"

  private var subscriptions: Map[String, Seq[ActorRef]] = Map.empty.withDefaultValue(Seq.empty)

  def state() = Action{
    Ok(repo.get("default").asJson)
  }

  def sendCommand() = Action(circe.tolerantJson[Command]) { request =>
    repo.get(id) match{
      case Some(gameState) =>
        val logged = Processor(gameState)(Seq(request.body))
        repo.save(id, logged.state)
//        subscriptions(id).foreach(_ ! logged.events)
        Source[EventLog](logged.events.to[scala.collection.immutable.Iterable]).map(_.asJson.noSpaces).runWith(sink)
        Ok(logged.state.asJson)
      case None => NotFound
    }
  }

  private[this] val (sink, source) =
    MergeHub.source[String](perProducerBufferSize = 16)
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
      .run()


  def sse = Action {
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }


  @deprecated
  def ws() = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      Props(classOf[EventStreamActor],
        out,
        (self: ActorRef) => subscriptions += (id -> (self +: subscriptions(id))): Unit
      )
    }
  }
}

class EventStreamActor(out: ActorRef, initialize: ActorRef => Unit) extends Actor {
  initialize(self)

  private def sendToClients(event: EventLog): Unit = out ! event.asJson.noSpaces

  override def receive: Receive = {
    case events: Seq[EventLog] =>
      events.foreach(sendToClients)

    case event: EventLog =>
      sendToClients(event)
  }
}