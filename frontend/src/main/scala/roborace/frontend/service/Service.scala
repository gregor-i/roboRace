package roborace.frontend.service

import api.User
import gameEntities._
import io.circe.generic.auto._
import org.scalajs.dom.EventSource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Service extends ServiceTrait {

  def getAllGames(): Future[Seq[GameResponse]] =
    get("/api/games")
      .flatMap(check(200))
      .flatMap(parse[Seq[GameResponse]])

  def postGame(scenario: Scenario, index: Int): Future[GameResponse] =
    post(s"/api/games?index=$index", scenario)
      .flatMap(check(201))
      .flatMap(parse[GameResponse])

  def postCommand(gameId: String, command: Command): Future[GameResponse] =
    post(s"/api/games/$gameId/commands", command)
      .flatMap(check(200))
      .flatMap(parse[GameResponse])

  def getAllScenarios(): Future[Seq[ScenarioResponse]] =
    get("/api/scenarios")
      .flatMap(check(200))
      .flatMap(parse[Seq[ScenarioResponse]])

  def postScenario(scenario: ScenarioPost): Future[ScenarioResponse] =
    post("/api/scenarios", scenario)
      .flatMap(check(201))
      .flatMap(parse[ScenarioResponse])

  def deleteScenario(scenario: ScenarioResponse): Future[Unit] =
    delete(s"/api/scenarios/${scenario.id}")
      .flatMap(check(204))
      .map(_ => ())

  def whoAmI(): Future[User] =
    get("/api/users/me")
      .flatMap(check(200))
      .flatMap(parse[User])

  def lobbyUpdates(): EventSource = new EventSource("/api/games/events")

  def gameUpdates(gameId: String): EventSource = new EventSource(s"/api/games/$gameId/events")
}
