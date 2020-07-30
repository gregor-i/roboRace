package roborace.frontend.lobby

import gameEntities._
import roborace.frontend.FrontendState
import roborace.frontend.components.{Tag, Card, Column, Images, MediaObject, RobotImage}
import roborace.frontend.editor.EditorState
import roborace.frontend.game.GameState
import roborace.frontend.preview.PreviewState
import snabbdom.Node

object LobbyUi {
  def render(lobbyState: LobbyState, update: FrontendState => Unit): Node =
    Node("div.robo-race")
      .child(renderHeader())
      .child(
        Column(
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
      case Some(_: QuittedPlayer)                                   => Some(Tag("Quitted", "is-danger"))
      case Some(you: FinishedPlayer)                                => Some(Tag(s"Finished as ${you.rank}", "is-primary"))
      case Some(you: RunningPlayer) if you.instructionSlots.isEmpty => Some(Tag("Awaits your instructions", "is-warning"))
      case Some(_: RunningPlayer)                                   => Some(Tag("Awaiting other players instructions"))
      case _                                                        => None
    }

    val sizeTag = Tag(s"Size: ${gameResponse.robots.size} / ${gameResponse.scenario.initialRobots.size} players", "is-info")

    val joinTag = Some(Tag("Open for new player", "is-primary"))
      .filter(_ => gameResponse.robots.size < gameResponse.scenario.initialRobots.size && gameResponse.cycle == 0)

    Card(
      MediaObject(
        Some(RobotImage(gameResponse.id.hashCode().abs % 6, filled = true)),
        Node("div.tags.are-large")
          .child(sizeTag)
          .childOptional(youTag)
          .childOptional(joinTag)
      ),
      "Enter" -> Some(_ => update(GameState(gameResponse))),
      "Quit"  -> None
    )
  }

  def scenarioCard(scenarioResponse: ScenarioResponse, update: FrontendState => Unit): Node =
    Card(
      MediaObject(
        Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
        Node("div.tags.are-large")
          .child(Tag(s"Description: ${scenarioResponse.description}", "is-info"))
          .child(Tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"))
          .childOptional(if (scenarioResponse.scenario.traps.nonEmpty) Some(Tag(s"Contains traps", "is-warning")) else None)
          .childOptional(if (scenarioResponse.ownedByYou) Some(Tag(s"Created by you", "is-info")) else None)
      ),
      "Start Game" -> Some(_ => update(PreviewState(scenarioResponse))),
      "Editor"     -> Some(_ => update(EditorState(scenarioResponse.scenario, scenarioResponse.description))),
      "Delete"     -> None
    )

}
