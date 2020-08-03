package roborace.frontend.service

import gameEntities._
import io.circe.generic.auto._
import org.scalajs.dom.EventSource
import roborace.frontend.toasts.Syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Service extends ServiceTrait {

  def getAllGames(): Future[Seq[GameResponse]] =
    get("/api/games")
      .flatMap(check(200))
      .flatMap(parse[Seq[GameResponse]])

  def getAllScenarios(): Future[Seq[ScenarioResponse]] =
    get("/api/scenarios")
      .flatMap(check(200))
      .flatMap(parse[Seq[ScenarioResponse]])

  def lobbyUpdates(): EventSource = new EventSource("/api/games/events")

  def gameUpdates(gameId: String): EventSource = new EventSource(s"/api/games/$gameId/events")

  def createGame(scenario: Scenario, index: Int): Future[GameResponse] =
    withSuccessToast("Starting Game", "Game started") {
      post(s"/api/games?index=$index", scenario)
        .flatMap(check(201))
        .flatMap(parse[GameResponse])
    }

  def sendCommand(gameId: String, command: Command): Future[GameResponse] =
    withSuccessToast("Sending Instructions to Server", "Instructions send to Server") {
      post(s"/api/games/$gameId/commands", command)
        .flatMap(check(200))
        .flatMap(parse[GameResponse])
    }

  def saveScenario(scenario: ScenarioPost): Future[ScenarioResponse] =
    withSuccessToast("Saving Scenario", "Scenario saved") {
      post("/api/scenarios", scenario)
        .flatMap(check(200))
        .flatMap(parse[ScenarioResponse])
    }
}
