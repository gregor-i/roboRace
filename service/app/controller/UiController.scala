package controller

import controllers.Assets
import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository}

class UiController @Inject() (sessionAction: SessionAction, gameRepo: GameRepository, scenarioRepo: ScenarioRepository, assets: Assets)
    extends InjectedController {

  def lobby(path: String) = sessionAction { (session, _) =>
    Ok(views.html.RoboRace(session.id))
  }

  def asset(path: String) = assets.at(path)
}
