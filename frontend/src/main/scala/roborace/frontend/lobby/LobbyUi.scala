package roborace.frontend.lobby

import gameEntities._
import roborace.frontend.{FrontendState, GameFrontendState, LobbyFrontendState, PreviewFrontendState}
import roborace.frontend.components.{BulmaComponents, Images, RobotImage}
import snabbdom.Node

object LobbyUi {
  def render(lobbyState: LobbyFrontendState, update: FrontendState => Unit): Node =
    Node("div.robo-race")
      .child(renderHeader())
      .child(
        BulmaComponents.singleColumn(
          lobbyState.games.map(gameCard(_, update)) ++ lobbyState.scenarios.map(scenarioCard(_, update))
        )
      )

  def renderHeader(): Node =
    Node("nav.navbar.is-light")
      .child(
        Node("div")
          .classes("navbar-brand")
          .child(
            Node("a")
              .classes("navbar-item")
              .attr("href", "/")
              .child(Node("img").attr("src", Images.logo))
          )
      )
      .child(
        Node("a.navbar-item")
          .attr("href", "#")
          .text("Tutorial")
      )

  def gameCard(gameResponse: GameResponse, update: FrontendState => Unit): Node = {
    val youTag: Option[Node] = gameResponse.you match {
      case Some(_: QuittedPlayer)                                   => Some(BulmaComponents.tag("Quitted", "is-danger"))
      case Some(you: FinishedPlayer)                                => Some(BulmaComponents.tag(s"Finished as ${you.rank}", "is-primary"))
      case Some(you: RunningPlayer) if you.instructionSlots.isEmpty => Some(BulmaComponents.tag("Awaits your instructions", "is-warning"))
      case Some(_: RunningPlayer)                                   => Some(BulmaComponents.tag("Awaiting other players instructions"))
      case _                                                        => None
    }

    val sizeTag = BulmaComponents.tag(s"Size: ${gameResponse.robots.size} / ${gameResponse.scenario.initialRobots.size} players", "is-info")

    val joinTag = Some(BulmaComponents.tag("Open for new player", "is-primary"))
      .filter(_ => gameResponse.robots.size < gameResponse.scenario.initialRobots.size && gameResponse.cycle == 0)

    BulmaComponents.card(
      BulmaComponents.mediaObject(
        Some(RobotImage(gameResponse.id.hashCode().abs % 6, filled = true)),
        Node("div.tags.are-large")
          .child(sizeTag)
          .childOptional(youTag)
          .childOptional(joinTag)
      ),
      "Enter" -> Some(_ => update(GameFrontendState(gameResponse, 0, Map.empty))),
      "Quit"  -> None
    )
  }

  def scenarioCard(scenarioResponse: ScenarioResponse, update: FrontendState => Unit): Node =
    BulmaComponents.card(
      BulmaComponents
        .mediaObject(
          Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
          Node("div.tags.are-large")
            .child(BulmaComponents.tag(s"Description: ${scenarioResponse.description}", "is-info"))
            .child(BulmaComponents.tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"))
            .childOptional(if (scenarioResponse.scenario.traps.nonEmpty) Some(BulmaComponents.tag(s"Contains traps", "is-warning")) else None)
            .childOptional(if (scenarioResponse.ownedByYou) Some(BulmaComponents.tag(s"Created by you", "is-info")) else None)
        ),
      "Start Game" -> Some(_ => update(PreviewFrontendState(scenarioResponse))),
      "Editor"     -> None, //Some(_ => Main.gotoEditor(scenarioResponse)),
      "Delete"     -> None
    )

}
