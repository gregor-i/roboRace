package controller

import javax.inject.Inject

import play.api.mvc.InjectedController
import repo.GameRepository


class FrontendController @Inject()(gameRepo: GameRepository) extends InjectedController{

  def index() = Action(
    Redirect("/game/default", PERMANENT_REDIRECT)
  )

  def lobby() = Action{
    Ok(views.html.Lobby(gameRepo.list()))
  }

  def game(player: String) = Action {
    Ok(views.html.Game(player))
  }
}
