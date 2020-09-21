package roborace.frontend.pages.multiplayer.lobby

import api.{GameResponse, ScenarioResponse}
import entities._
import roborace.frontend.FrontendState
import roborace.frontend.pages.components._
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.preview.PreviewState
import snabbdom.{Node, Snabbdom}
import roborace.frontend.pages.editor.EditorState
import roborace.frontend.service.Actions
import roborace.frontend.util.SnabbdomEventListener

object LobbyUi {
  def render(implicit lobbyState: LobbyState, update: FrontendState => Unit): Node =
    Body()
      .child(Header())
      .child(
        Column(
          lobbyState.games.map(gameCard(_)) ++ lobbyState.scenarios.map(scenarioCard(_))
        )
      )

  def gameCard(gameResponse: GameResponse)(implicit state: LobbyPage.State, update: LobbyPage.Update): Node = {
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
        Node("div").children(
          Node("div.tags")
            .child(sizeTag)
            .childOptional(youTag)
            .childOptional(joinTag),
          ButtonList()
            .childOptional(
              if (gameResponse.ownedByYou)
                Some(Button("Delete Game", SnabbdomEventListener.sideeffect(() => Actions.deleteGame(gameResponse))))
              else
                None
            )
            .child(
              Button("Join Game", SnabbdomEventListener.sideeffect(() => update(GameState(gameResponse)))).classes("is-primary")
            )
        )
      )
    ).classes("has-background-light")
  }

  def scenarioCard(scenarioResponse: ScenarioResponse)(implicit state: LobbyState, update: FrontendState => Unit): Node =
    Card(
      MediaObject(
        Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
        Node("div").children(
          Node("h4.title").text(scenarioResponse.description),
          Node("div.tags")
            .child(Tag(s"Size: ${scenarioResponse.scenario.initialRobots.size} players", "is-info"))
            .childOptional(if (scenarioResponse.scenario.traps.nonEmpty) Some(Tag(s"Contains traps", "is-warning")) else None)
            .childOptional(if (scenarioResponse.ownedByYou) Some(Tag(s"Created by you", "is-info")) else None),
          ButtonList()
            .childOptional(
              if (scenarioResponse.ownedByYou)
                Some(Button("Delete Scenario", SnabbdomEventListener.sideeffect(() => Actions.deleteScenario(scenarioResponse))))
              else
                None
            )
            .children(
              Button("Edit Scenario", SnabbdomEventListener.set(EditorState(scenarioResponse))),
              Button("Start Game", SnabbdomEventListener.set(update(PreviewState(scenarioResponse)))).classes("is-primary")
            )
        )
      )
    ).classes("has-background-light")

}
