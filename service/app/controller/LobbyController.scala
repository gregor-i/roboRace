package controller

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.Scenario
import gameLogic.command.{CommandAccepted, CommandRejected, CreateGame}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import model.GameResponse
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{GameRepository, GameRow, Session}

import scala.concurrent.ExecutionContext

@Singleton
class LobbyController @Inject()(sessionAction: SessionAction,
                                gameRepo: GameRepository)
                               (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe with JsonUtil {

  private val (sink, source) = new SinkSourceCache[Seq[GameRow]].createPair()

  def list() = sessionAction {(session, _) =>
    Ok(gameList().map(GameResponse(_)(session)).asJson)
  }

  def create() = sessionAction(circe.tolerantJson[Scenario]) { (session, request) =>
    CreateGame(request.body)(session.playerId) match {
      case CommandRejected(reason) =>
        BadRequest(reason.asJson)
      case CommandAccepted(game)   =>
        val row = GameRow(
          id = Utils.newId(),
          owner = session.playerId,
          game = Some(game),
          creationTime = ZonedDateTime.now()
        )
        gameRepo.save(row)
        sendStateToClients()
        Created(GameResponse(game, row.id)(session).asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    gameRepo.get(id) match {
      case None => NotFound
      case Some(row) if row.owner != session.playerId => Unauthorized
      case Some(_) =>
        gameRepo.delete(id)
        sendStateToClients()
        NoContent
    }
  }

  def sendStateToClients() =
    Source.single(gameList()).runWith(sink)

  def sse() = sessionAction { (session, _) =>
    Ok.chunked(
      source
        .map(games => games.flatMap(GameResponse(_)(session)))
        .map(_.asJson.noSpaces)
        .via(EventSource.flow)
    ).as(ContentTypes.EVENT_STREAM)
  }

  private def gameList(): Seq[GameRow] = gameRepo.list()
}
