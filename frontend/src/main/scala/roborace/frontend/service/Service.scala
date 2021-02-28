package roborace.frontend.service

import api.{User, WithId}
import entities._
import io.circe.generic.auto._
import logic.command.Command
import org.scalajs.dom.EventSource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Service extends ServiceTrait {

  def getSessionId(): Future[String] =
    get("/api/session")
      .flatMap(check(200))
      .flatMap(parse[String])

  def getAllGames(): Future[Seq[WithId[Game]]] =
    get("/api/games")
      .flatMap(check(200))
      .flatMap(parse[Seq[WithId[Game]]])

  def postGame(scenario: Scenario, index: Int): Future[WithId[Game]] =
    post(s"/api/games?index=$index", scenario)
      .flatMap(check(201))
      .flatMap(parse[WithId[Game]])

  def deleteGame(gameId: String): Future[Unit] =
    delete(s"/api/games/${gameId}")
      .flatMap(check(204))
      .map(_ => ())

  def postCommand(gameId: String, command: Command): Future[WithId[Game]] =
    post(s"/api/games/$gameId/commands", command)
      .flatMap(check(200))
      .flatMap(parse[WithId[Game]])

  def getScenario(scenarioId: String): Future[WithId[Scenario]] =
    get(s"/api/scenarios/${scenarioId}")
      .flatMap(check(200))
      .flatMap(parse[WithId[Scenario]])

  def getAllScenarios(): Future[Seq[WithId[Scenario]]] =
    get("/api/scenarios")
      .flatMap(check(200))
      .flatMap(parse[Seq[WithId[Scenario]]])

  def postScenario(scenario: Scenario): Future[WithId[Scenario]] =
    post("/api/scenarios", scenario)
      .flatMap(check(201))
      .flatMap(parse[WithId[Scenario]])

  def deleteScenario(scenario: WithId[Scenario]): Future[Unit] =
    delete(s"/api/scenarios/${scenario.id}")
      .flatMap(check(204))
      .map(_ => ())

  def lobbyUpdates(): EventSource = new EventSource("/api/games/events")

  def gameUpdates(gameId: String): EventSource = new EventSource(s"/api/games/$gameId/events")
}
