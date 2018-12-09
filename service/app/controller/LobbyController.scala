package controller

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gameLogic.command.{CommandAccepted, CommandRejected, CreateGame}
import gameLogic.{Game, Scenario}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{GameRepository, GameRow}

import scala.concurrent.ExecutionContext


class LobbyController @Inject()(sessionAction: SessionAction,
                                gameRepo: GameRepository)
                               (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe with JsonUtil {

  private val (sink, source) = SinkSourceCache.createPair()

  def list() = Action {
    Ok(gameList().asJson)
  }

  def create() = sessionAction(circe.tolerantJson[Scenario]) { (session, request) =>
    CreateGame(request.body)(session.id) match {
      case CommandRejected(reason) =>
        BadRequest(reason.asJson)
      case CommandAccepted(game)   =>
        val row = GameRow(
          id = Utils.newId(),
          owner = session.id,
          game = Some(game),
          creationTime = ZonedDateTime.now()
        )
        gameRepo.save(row)
        Source.single(gameList().asJson.noSpaces).runWith(sink)
        Created(row.asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    gameRepo.get(id) match {
      case None => NotFound
      case Some(row) if row.owner != session.id => Unauthorized
      case Some(_) =>
        gameRepo.delete(id)
        Source.single(gameList().asJson.noSpaces).runWith(sink)
        NoContent
    }
  }

  def sse() = Action {
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }

  def stateDescription(gameState:Game): String = gameState match{
    case game if game.cycle == 0 && game.players.length < game.scenario.initialRobots.length =>
      s"Open for new Player. ${game.players.length} / ${game.scenario.initialRobots.length}"
    case game if game.cycle != 0 && game.players.forall(_.finished.isDefined) =>
      s"Game finished"
    case _ =>
      s"Game in Progress"
  }

  def gameList() =
    gameRepo.list()
      .filter(_.game.isDefined)
      .map(row => GameOverview(row.id, row.owner, stateDescription(row.game.get)))
}

case class GameOverview(id: String, owner: String, state: String)
