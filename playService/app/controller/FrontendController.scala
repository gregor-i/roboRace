package controller

import javax.inject.Inject

import gameLogic.GameRunning
import play.api.mvc.InjectedController
import repo.GameRepository


class FrontendController @Inject()(gameRepo: GameRepository) extends InjectedController{

  val id = "default"

  def index() = Action(
    Redirect("/game/default", PERMANENT_REDIRECT)
  )

  def lobby() = Action{
    Ok(views.html.Lobby(gameRepo.list()))
  }

  def game(player: String) = Action {
    gameRepo.get(id) match {
      case Some(game: GameRunning) => Ok(views.html.Game(player, game.scenario))
      case Some(_) => BadRequest
      case None => NotFound
    }
  }
}
