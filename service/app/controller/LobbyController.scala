package controller

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


class LobbyController @Inject()(gameRepo: GameRepository)
                               (implicit system: ActorSystem, mat: Materializer, ex: ExecutionContext)
  extends InjectedController with Circe {

  private val (sink, source) = SinkSourceCache.createPair()

  def list() = Action {
    Ok(gameList().asJson)
  }

  def create() = Action(circe.tolerantJson[Scenario]) { request =>
    Utils.playerName(request) match {
      case None => Unauthorized
      case Some(player) =>
        CreateGame(request.body)(player) match {
          case CommandRejected(reason) =>
            BadRequest(reason.asJson)
          case CommandAccepted(game) =>
            val row = GameRow(id = Utils.newShortId(), owner = player, game = Some(game))
            gameRepo.save(row)
            Source.single(gameList().asJson.noSpaces).runWith(sink)
            Created(row.asJson)
        }
    }
  }

  def delete(id: String) = Action { request =>
    (gameRepo.get(id), Utils.playerName(request)) match {
      case (None, _) => NotFound
      case (_, None) => Unauthorized
      case (Some(row), Some(player)) if row.owner != player => Unauthorized
      case (Some(_), Some(_)) =>
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
