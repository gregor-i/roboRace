package controller

import controllers.Assets
import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository}

import scala.concurrent.ExecutionContext

class UiController @Inject() (assets: Assets)(implicit ex: ExecutionContext) extends InjectedController {

  def lobby(path: String) = assets.at("index.html")

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
