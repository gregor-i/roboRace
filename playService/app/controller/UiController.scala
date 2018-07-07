package controller

import controllers.Assets
import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository}

class UiController @Inject()(gameRepo: GameRepository,
                             scenarioRepo: ScenarioRepository,
                             assets: Assets) extends InjectedController {

  private def ui(mode: String, gameId: String = "", scenarioId: String = "") =
    Ok(views.html.RoboRace(mode = mode, gameId = gameId, scenarioId = scenarioId))

  def lobby() = Action(ui(mode = "lobby"))

  def game(id: String) = Action {
    if (gameRepo.get(id).flatMap(_.game).isDefined)
      ui(mode = "game", gameId = id)
    else
      NotFound(views.html.NotFound())
  }

  def editor(id: String) = Action {
    if(scenarioRepo.get(id).flatMap(_.scenario).isDefined)
      ui(mode = "editor", scenarioId = id)
    else
      NotFound(views.html.NotFound())
  }

  def asset(path: String) = assets.at(path)
}
