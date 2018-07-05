package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.gameUpdate._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository

import scala.concurrent.ExecutionContext

@Singleton
class GameController @Inject()(repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  def state(id: String) = Action {
    repo.get(id) match {
      case Some(game) => Ok(game.asJson)
      case None => NotFound
    }
  }

  def sendCommand(id: String) = Action(circe.tolerantJson[Command]) { request =>
    (repo.get(id), Utils.playerName(request)) match {
      case (Some(row), Some(player)) if row.game.isDefined =>
        val command = request.body
        command(player)(row.game.get) match {
          case CommandAccepted(afterCommand) =>
            val afterCycle = Cycle(afterCommand)
            repo.save(row.copy(game = Some(afterCycle.state)))
            Source.single(
              Json.obj(
                "state" -> afterCycle.state.asJson,
                "events" -> afterCycle.events.asJson,
                "textLog" -> afterCycle.events.map(_.text).asJson
              ).noSpaces)
              .runWith(SinkSourceCache.sink(id))
            Ok(afterCycle.asJson)
          case CommandRejected(reason) =>
            BadRequest(reason.asJson)
        }
      case (_, None) => Unauthorized
      case (None, _) => NotFound
      case (Some(row), _) if row.game.isEmpty => NotFound
    }
  }

  def sse(id: String) = Action {
    Ok.chunked(SinkSourceCache.source(id) via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
