package controller

import com.typesafe.config.ConfigValue
import gameLogic.Scenario
import io.circe.Encoder
import io.circe.syntax._
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import repo.{GameRepository, ScenarioRepository, ScenarioRow}

class AdminController @Inject()(configuration: Configuration,
                                scenarioRepo: ScenarioRepository,
                                gameRepo: GameRepository) extends InjectedController with Circe {
  implicit val encodeConfigVal: Encoder[ConfigValue] = Encoder.encodeString.contramap[ConfigValue](_.unwrapped.toString)

  def ui() = Action{
    Ok(views.html.Admin(configuration))
  }

  def postDefault() = Action{
    scenarioRepo.save(ScenarioRow(Utils.newId(), "system", "default", Some(Scenario.default)))
    NoContent
  }

  def garbageCollect() = Action{
    val deletedScenarios = scenarioRepo.list()
      .filter(row => row.scenario.fold(true)(sc => !Scenario.validation(sc)))
      .map(row => scenarioRepo.delete(row.id))
      .sum

    val deletedGames = gameRepo.list()
      .filter(row => row.game.isEmpty)
      .map(row => gameRepo.delete(row.id))
      .sum

    Ok(s"deleted: $deletedScenarios scenarios, $deletedGames games")
  }
}
