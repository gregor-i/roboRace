package controller

import gameLogic.Scenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{ScenarioRepository, ScenarioRow}

@Singleton
class ScenarioController @Inject()(repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = Action {
    val list = repo.list()
    if (list.isEmpty) {
      val defaultRow = ScenarioRow("default", "system", Some(Scenario.default))
      repo.save(defaultRow)
      Ok(List(defaultRow).asJson)
    } else {
      Ok(list.asJson)
    }
  }

  def getSingle(id: String) = Action {
    repo.get(id) match {
      case None => NotFound
      case Some(row) => Ok(row.asJson)
    }
  }

  def post() = Action(circe.tolerantJson[Scenario]) { request =>
    Utils.playerName(request) match {
      case None => Unauthorized
      case _ if !Scenario.validation(request.body) => BadRequest
      case Some(player) =>
        val row = ScenarioRow(
          id = Utils.newShortId(),
          owner = player,
          scenario = Some(request.body))
        repo.save(row)
        Created(row.asJson)
    }
  }

  def delete(id: String) = Action { request =>
    (repo.get(id), Utils.playerName(request)) match {
      case (None, _) => NotFound
      case (_, None) => Unauthorized
      case (Some(row), Some(player)) if row.owner != player => Unauthorized
      case (Some(row), Some(_)) => repo.delete(row.id)
        NoContent
    }
  }

  // todo: SSE!

  def image(id: String) = Action {
    repo.get(id) match {
      case Some(ScenarioRow(_, _, Some(scenario))) => Ok(svg.xml.Scenario(scenario, false)).as("image/svg+xml")
      case _                                       => NotFound
    }
  }
}
