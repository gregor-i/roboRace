package controller

import javax.inject.Inject

import gameLogic.{GameNotStarted, GameRunning, GameScenario}
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.GameRepository


class FrontendController @Inject()(gameRepo: GameRepository) extends InjectedController with Circe{

  def index() = Action(
    Ok(views.html.Lobby(gameRepo.list()))
  )

  def game(id: String, player: String) = Action {
    gameRepo.get(id) match {
      case Some(game: GameRunning) => Ok(views.html.Game(id, player, game.scenario))
      case Some(game: GameNotStarted) => Ok(views.html.Game(id, player, game.scenario))
      case Some(_) => BadRequest
      case None => NotFound
    }
  }

  def defaultScenario() = Action{
    Ok(GameScenario.default.asJson)
  }
}
