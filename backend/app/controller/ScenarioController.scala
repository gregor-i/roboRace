package controller

import api.Entity
import entities.Scenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import logic.{DefaultScenario, ValidateScenario}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.ScenarioRepository

@Singleton
class ScenarioController @Inject() (sessionAction: SessionAction, repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = sessionAction { (session, request) =>
    val list = repo.list().collect(repo.rowToEntity)

    if (list.isEmpty) {
      val defaultRow = Entity(title = "default", value = DefaultScenario.default)
      repo.save(id = Utils.newId(), owner = "system", entity = defaultRow)
      val list = repo.list().collect(repo.rowToEntity)
      Ok(list.asJson)
    } else {
      Ok(list.asJson)
    }
  }

  def getSingle(id: String) = sessionAction { (session, request) =>
    repo.get(id).collect(repo.rowToEntity) match {
      case None      => NotFound
      case Some(row) => Ok(row.asJson)
    }
  }

  def post() = sessionAction(circe.tolerantJson[Entity[Scenario]]) { (session, request) =>
    if (ValidateScenario(request.body.value)) {
      val withId = repo.save(id = Utils.newId(), owner = session.id, entity = request.body)
      Created(withId.asJson)
    } else {
      BadRequest
    }
  }

  def put(id: String) = sessionAction(circe.tolerantJson[Entity[Scenario]]) { (session, request) =>
    repo.get(id) match {
      case _ if !ValidateScenario(request.body.value)           => BadRequest
      case Some(scenarioRow) if scenarioRow.owner != session.id => Forbidden
      case _ =>
        val withId = repo.save(id = id, owner = session.id, entity = request.body)
        Ok(withId.asJson)
    }
  }

  def delete(id: String) = sessionAction { (session, request) =>
    repo.get(id) match {
      case None                                 => NotFound
      case Some(row) if row.owner != session.id => Unauthorized
      case Some(_) =>
        repo.delete(id)
        NoContent
    }
  }
}
