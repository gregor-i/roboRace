package roborace.frontend.service

import gameEntities._
import io.circe.generic.auto._
import org.scalajs.dom
import org.scalajs.dom.EventSource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Service extends ServiceTrait {
  private def withLoadingOverlay[A](f: => Future[A]): Future[A] = {
    dom.document.getElementById("loading-overlay").classList.add("is-active")
    val fut = f
    fut.failed.foreach(System.err.println)
    fut.onComplete(_ => dom.document.getElementById("loading-overlay").classList.remove("is-active"))
    fut
  }

  def getAllGames(): Future[Seq[GameResponse]] =
    withLoadingOverlay {
      get("/api/games")
        .flatMap(check(200))
        .flatMap(parse[Seq[GameResponse]])
    }

  def getAllScenarios(): Future[Seq[ScenarioResponse]] =
    withLoadingOverlay {
      get("/api/scenarios")
        .flatMap(check(200))
        .flatMap(parse[Seq[ScenarioResponse]])
    }

  def lobbyUpdates(): EventSource = new EventSource("/api/games/events")

  def gameUpdates(gameId: String): EventSource = new EventSource(s"/api/games/$gameId/events")

  def createGame(scenario: Scenario, index: Int): Future[GameResponse] =
    withLoadingOverlay {
      post(s"/api/games?index=$index", scenario)
        .flatMap(check(200))
        .flatMap(parse[GameResponse])
    }

  def sendCommand(gameId: String, command: Command): Future[GameResponse] =
    withLoadingOverlay {
      post(s"/api/games/$gameId/commands", command)
        .flatMap(check(200))
        .flatMap(parse[GameResponse])
    }

  def saveScenario(scenario: ScenarioPost): Future[ScenarioResponse] =
    withLoadingOverlay {
      post("/api/scenarios", scenario)
        .flatMap(check(200))
        .flatMap(parse[ScenarioResponse])
    }
}
