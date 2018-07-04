package controller

import gameLogic.GameScenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{ScenarioRepository, ScenarioRow}

@Singleton
class ScenarioController @Inject()(repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = Action {
    Ok(repo.list().asJson)
  }

  def post() = Action(circe.tolerantJson[GameScenario]) { request =>
    Utils.playerName(request) match {
      case None => Unauthorized
      case _ if !GameScenario.validation(request.body) => BadRequest
      case Some(player) =>
        val row = ScenarioRow(
          id = Utils.newShortId(),
          owner = player,
          scenario = Some(request.body))
        repo.save(row)
        Created(row.asJson)
    }
  }

  def delete(id: String) = Action{ request =>
    (repo.get(id), Utils.playerName(request)) match {
      case (None, _) => NotFound
      case (_, None) => Unauthorized
      case (Some(row), Some(player)) if row.owner != player => Unauthorized
      case (Some(row), Some(_)) => repo.delete(row.id)
        NoContent
    }
  }

  // todo: SSE!
}
