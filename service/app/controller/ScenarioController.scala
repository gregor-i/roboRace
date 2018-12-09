package controller

import gameLogic.Scenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{ScenarioRepository, ScenarioRow}

case class ScenarioPost(description: String, scenario: Scenario)

@Singleton
class ScenarioController @Inject()(sessionAction: SessionAction,
                                   repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = Action {
    val list = repo.list().filter(_.scenario.isDefined)
    if (list.isEmpty) {
      val defaultRow = ScenarioRow(Utils.newId(), "system", "default", Some(Scenario.default))
      repo.save(defaultRow)
      Ok(List(defaultRow).asJson)
    } else {
      Ok(list.asJson)
    }
  }

  def getSingle(id: String) = Action {
    repo.get(id) match {
      case None      => NotFound
      case Some(row) => Ok(row.asJson)
    }
  }

  def post() = sessionAction(circe.tolerantJson[ScenarioPost]) { (session, request) =>
    if (Scenario.validation(request.body.scenario)) {
      val row = ScenarioRow(
        id = Utils.newId(),
        owner = session.playerId,
        description = request.body.description,
        scenario = Some(request.body.scenario))
      repo.save(row)
      Created(row.asJson)
    } else {
      BadRequest
    }
  }

  def put(id: String) = sessionAction(circe.tolerantJson[ScenarioPost]) { (session, request) =>
    repo.get(id) match {
      case _ if !Scenario.validation(request.body.scenario)           => BadRequest
      case Some(scenarioRow) if scenarioRow.owner != session.playerId => Forbidden
      case _                                                          =>
        val row = ScenarioRow(
          id = id,
          owner = session.playerId,
          description = request.body.description,
          scenario = Some(request.body.scenario))
        repo.save(row)
        Ok(row.asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    repo.get(id) match {
      case None                                       => NotFound
      case Some(row) if row.owner != session.playerId => Unauthorized
      case Some(row)                                  => repo.delete(id)
        NoContent
    }
  }

  // todo: SSE!
}
