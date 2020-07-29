package roborace.frontend.service

import gameEntities._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.{EventSource, XMLHttpRequest}

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

  private def checkAndParse[A: Decoder](expected: Int)(req: XMLHttpRequest): A =
    decode[A](req.responseText).right.get

  private implicit def encode[A: Encoder](a: A): InputData =
    a.asJson.noSpaces.asInstanceOf[InputData]

  def getAllGames(): Future[Seq[GameResponse]] =
    withLoadingOverlay {
      Ajax
        .get("/api/games", withCredentials = true)
        .map(checkAndParse[Seq[GameResponse]](200))
    }

  def getAllScenarios(): Future[Seq[ScenarioResponse]] =
    withLoadingOverlay {
      Ajax
        .get("/api/scenarios", withCredentials = true)
        .map(checkAndParse[Seq[ScenarioResponse]](200))
    }

  def lobbyUpdates(): EventSource = new EventSource("/api/games/events")

  def gameUpdates(gameId: String): EventSource = new EventSource(s"/api/games/$gameId/events")

  def createGame(scenario: Scenario, index: Int): Future[GameResponse] =
    withLoadingOverlay {
      Ajax
        .post(s"/api/games?index=$index", withCredentials = true, data = scenario, headers = Map("Content-Type" -> "application/json; charset=UTF-8"))
        .map(checkAndParse[GameResponse](200))
    }

  def sendCommand(gameId: String, command: Command): Future[GameResponse] =
    withLoadingOverlay {
      Ajax
        .post(
          s"/api/games/$gameId/commands",
          withCredentials = true,
          data = command,
          headers = Map("Content-Type" -> "application/json; charset=UTF-8")
        )
        .map(checkAndParse[GameResponse](200))
    }

  def saveScenario(scenario: ScenarioPost): Future[ScenarioResponse] =
    withLoadingOverlay {
      Ajax
        .post("/api/scenarios", withCredentials = true, data = scenario, headers = Map("Content-Type" -> "application/json; charset=UTF-8"))
        .map(checkAndParse[ScenarioResponse](201))
    }
}
