package controller

import java.util.UUID

import gameLogic.GameScenario
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.ScenarioRepository

@Singleton
class ScenarioController @Inject()(repo: ScenarioRepository) extends InjectedController with Circe {

  def get() = Action {
    Ok(repo.list().toMap.asJson)
  }

  def post() = Action(circe.tolerantJson[GameScenario]) { request =>
    val id = UUID.randomUUID().toString
    val scenario = request.body
    repo.save(id, scenario)
    Ok
  }

  def postDefault() = Action{
    repo.save("default", GameScenario.default)
    Ok
  }
}
