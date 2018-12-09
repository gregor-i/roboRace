package controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.Game
import gameLogic.command.{Command, CommandAccepted, CommandRejected}
import gameLogic.gameUpdate._
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import model.GameResponse
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{GameRepository, GameRow}

import scala.concurrent.ExecutionContext

@Singleton
class GameController @Inject()(sessionAction: SessionAction,
                               lobbyController: LobbyController,
                               repo: GameRepository)
                              (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe with JsonUtil {

  val sseCache = new SinkSourceCache[Game]

  def state(id: String) = sessionAction { (session, _) =>
    repo.get(id) match {
      case Some(GameRow(_, _, Some(game), _)) => Ok(GameResponse(game, id)(session).asJson)
      case _                                  => NotFound
    }
  }

  def sendCommand(id: String) = sessionAction(circe.tolerantJson[Command]) { (session, request) =>
    repo.get(id) match {
      case Some(row) if row.game.isDefined =>
        val command = request.body
        command(session.playerId)(row.game.get) match {
          case CommandAccepted(afterCommand) =>
            val afterCycle = Cycle(afterCommand)
            repo.save(row.copy(game = Some(afterCycle)))
            if(afterCycle.events != row.game.get.events)
              Source.single(afterCycle).runWith(sseCache.sink(id))
            if(afterCycle.events != row.game.get.events && afterCycle.cycle == 0)
              lobbyController.sendStateToClients()
            Ok(GameResponse(afterCycle, id)(session).asJson)
          case CommandRejected(reason) =>
            BadRequest(reason.asJson)
        }
      case None => NotFound
      case Some(row) if row.game.isEmpty => NotFound
    }
  }

  def sse(id: String) = sessionAction { (session, _) =>
    Ok.chunked(
      sseCache.source(id)
        .map(game => GameResponse(game, id)(session))
        .map(_.asJson.noSpaces)
        .via(EventSource.flow)
    ).as(ContentTypes.EVENT_STREAM)
  }
}
