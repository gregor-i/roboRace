package controller

import com.typesafe.config.ConfigValue
import gameLogic.GameScenario
import io.circe.Encoder
import io.circe.syntax._
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{ScenarioRepository, ScenarioRow}

class AdminController @Inject()(configuration: Configuration,
                                scenarioRepo: ScenarioRepository) extends InjectedController with Circe {
  implicit val encodeConfigVal: Encoder[ConfigValue] = Encoder.encodeString.contramap[ConfigValue](_.unwrapped.toString)

  def ui() = Action{
    NotImplemented
  }

  def config() = Action {
    Ok(configuration.entrySet.toMap.asJson)
  }

  def postDefault() = Action{
    scenarioRepo.save(ScenarioRow("default", "system", Some(GameScenario.default)))
    NoContent
  }

  def garbageCollect() = Action{
    val deletedScenarios = scenarioRepo.list()
      .filter(row => row.scenario.fold(true)(sc => !GameScenario.validation(sc)))
      .map(row => scenarioRepo.delete(row.id))
      .sum

    Ok(s"deleted: $deletedScenarios scenarios")
  }
}
