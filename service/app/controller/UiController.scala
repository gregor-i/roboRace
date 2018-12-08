package controller

import controllers.Assets
import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository, Session}

class UiController @Inject()(sessionAction: SessionAction,
                             gameRepo: GameRepository,
                             scenarioRepo: ScenarioRepository,
                             assets: Assets) extends InjectedController {

  private def ui(mode: String, gameId: String = "", scenarioId: String = "", session: Session) =
    Ok(views.html.RoboRace(mode = mode, gameId = gameId, scenarioId = scenarioId, sessionId = session.id))

  def lobby() = sessionAction { (session, _) =>
    ui(mode = "lobby", session = session)
  }

  def game(id: String) = sessionAction { (session, _) =>
    if (gameRepo.get(id).flatMap(_.game).isDefined)
      ui(mode = "game", gameId = id, session = session)
    else
      NotFound(views.html.NotFound())
  }

  def editor(id: String) = sessionAction { (session, _) =>
    if (scenarioRepo.get(id).flatMap(_.scenario).isDefined)
      ui(mode = "editor", scenarioId = id, session = session)
    else
      NotFound(views.html.NotFound())
  }

  def asset(path: String) = assets.at(path)
}
