package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import api.WithId
import entities.Game
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import logic.command.Command
import logic.gameUpdate._
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

@Singleton
class GameController @Inject() (sessionAction: SessionAction, lobbyController: LobbyController, repo: GameRepository)(
    implicit system: ActorSystem,
    mat: Materializer,
    ex: ExecutionContext
) extends InjectedController
    with Circe
    with JsonUtil {

  val sseCache = new SinkSourceCache[WithId[Game]]

  def state(id: String) = sessionAction { (session, _) =>
    repo.get(id).collect(repo.rowToEntity) match {
      case Some(game) => Ok(game.asJson)
      case _          => NotFound
    }
  }

  def sendCommand(id: String) = sessionAction(circe.tolerantJson[Command]) { (session, request) =>
    repo.get(id).collect(repo.rowToEntity) match {
      case Some(row) =>
        Command(request.body, session.id)(row.entity)
          .map(Cycle.apply) match {
          case Right(afterCommand) =>
            val withId = WithId(id = id, owner = row.owner, entity = afterCommand)
            repo.save(id = id, owner = row.owner, entity = afterCommand)
            Source.single(withId).runWith(sseCache.sink(id))
            lobbyController.sendStateToClients()
            Ok(withId.asJson)
          case Left(reason) =>
            BadRequest(reason.asJson)
        }
      case None => NotFound
    }
  }

  def sse(id: String) = sessionAction { (session, _) =>
    repo.get(id).collect(repo.rowToEntity) match {
      case Some(row) =>
        Ok.chunked(
            sseCache
              .source(id)
              .map(_.asJson.noSpaces)
              .via(EventSource.flow)
          )
          .as(ContentTypes.EVENT_STREAM)

      case None => NotFound
    }
  }
}
