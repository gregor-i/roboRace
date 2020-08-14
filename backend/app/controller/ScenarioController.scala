package controller

import api.ScenarioPost
import logic.{DefaultScenario, ValidateScenario}
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import model.ScenarioResponseFactory
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{ScenarioRepository, ScenarioRow}

@Singleton
class ScenarioController @Inject() (sessionAction: SessionAction, repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = sessionAction { (session, request) =>
    val list = repo.list().flatMap(ScenarioResponseFactory.apply(_)(session))

    if (list.isEmpty) {
      val defaultRow = ScenarioRow(Utils.newId(), "system", "default", Some(DefaultScenario.default))
      repo.save(defaultRow)
      Ok(List(defaultRow).flatMap(ScenarioResponseFactory.apply(_)(session)).asJson)
    } else {
      Ok(list.asJson)
    }
  }

  def getSingle(id: String) = sessionAction { (session, request) =>
    repo.get(id).flatMap(ScenarioResponseFactory.apply(_)(session)) match {
      case None      => NotFound
      case Some(row) => Ok(row.asJson)
    }
  }

  def post() = sessionAction(circe.tolerantJson[ScenarioPost]) { (session, request) =>
    if (ValidateScenario(request.body.scenario)) {
      val row =
        ScenarioRow(id = Utils.newId(), owner = session.playerId, description = request.body.description, scenario = Some(request.body.scenario))
      repo.save(row)
      Created(ScenarioResponseFactory(row)(session).asJson)
    } else {
      BadRequest
    }
  }

  def put(id: String) = sessionAction(circe.tolerantJson[ScenarioPost]) { (session, request) =>
    repo.get(id) match {
      case _ if !ValidateScenario(request.body.scenario)              => BadRequest
      case Some(scenarioRow) if scenarioRow.owner != session.playerId => Forbidden
      case _ =>
        val row = ScenarioRow(id = id, owner = session.playerId, description = request.body.description, scenario = Some(request.body.scenario))
        repo.save(row)
        Ok(ScenarioResponseFactory(row)(session).asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    repo.get(id) match {
      case None                                       => NotFound
      case Some(row) if row.owner != session.playerId => Unauthorized
      case Some(row) =>
        repo.delete(id)
        NoContent
    }
  }

  // todo: SSE!
}
