package controller

import controllers.Assets
import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository}

import scala.concurrent.ExecutionContext

class UiController @Inject() (sessionAction: SessionAction, assets: Assets)(implicit ex: ExecutionContext) extends InjectedController {

  def lobby(path: String) = sessionAction { (session, _) =>
    Ok(views.html.RoboRace(session.id))
  }

  def asset(path: String) = assets.at(path)

  def serviceWorker(file: String, folder: String) =
    Action.async(
      rq =>
        assets
          .at(folder, file)
          .apply(rq)
          .map(_.withHeaders("Service-Worker-Allowed" -> "/"))
    )
}
