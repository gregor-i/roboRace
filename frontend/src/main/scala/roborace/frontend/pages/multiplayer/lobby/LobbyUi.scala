package roborace.frontend.pages
package multiplayer.lobby

import api.WithId
import entities._
import roborace.frontend.pages.components._
import roborace.frontend.pages.editor.EditorState
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyPage.Context
import roborace.frontend.pages.multiplayer.preview.PreviewState
import roborace.frontend.service.Actions
import roborace.frontend.util.SnabbdomEventListener
import snabbdom.Node
import snabbdom.components.{Button, ButtonList}

object LobbyUi {
  def render(implicit context: Context): Node =
    Body()
      .child(Header())
      .child(
        Column(
          context.local.games.map(gameCard(_)) ++ context.local.scenarios.map(scenarioCard(_))
        )
      )

  def gameCard(gameResponse: WithId[Game])(implicit context: Context): Node = {
    val youTag: Option[Node] = gameResponse.entity.players.find(_.id == context.global.sessionId) match {
      case Some(_: QuitedPlayer)                                    => Some(Tag("Quitted", "is-danger"))
      case Some(you: FinishedPlayer)                                => Some(Tag(s"Finished as ${you.rank}", "is-primary"))
      case Some(you: RunningPlayer) if you.instructionSlots.isEmpty => Some(Tag("Awaits your instructions", "is-warning"))
      case Some(_: RunningPlayer)                                   => Some(Tag("Awaiting other players instructions"))
      case _                                                        => None
    }

    val sizeTag = Tag(s"Size: ${gameResponse.entity.players.size} / ${gameResponse.entity.scenario.initialRobots.size} players", "is-info")

    val joinTag = Some(Tag("Open for new player", "is-primary"))
      .filter(_ => gameResponse.entity.players.size < gameResponse.entity.scenario.initialRobots.size && gameResponse.entity.cycle == 0)

    Card(
      MediaObject(
        Some(RobotImage(gameResponse.id.hashCode().abs % 6, filled = true)),
        Node("div").children(
          Node("div.tags")
            .child(sizeTag)
            .childOptional(youTag)
            .childOptional(joinTag),
          ButtonList
            .left()
            .childOptional(
              if (gameResponse.owner == context.global.sessionId)
                Some(Button("Delete Game", SnabbdomEventListener.sideeffect(() => Actions.deleteGame(gameResponse))))
              else
                None
            )
            .child(
              Button("Join Game", SnabbdomEventListener.sideeffect(() => context.update(GameState(gameResponse)))).classes("is-primary")
            )
        )
      )
    ).classes("has-background-light")
  }

  def scenarioCard(scenarioResponse: WithId[Scenario])(implicit context: Context): Node =
    Card(
      MediaObject(
        Some(RobotImage(scenarioResponse.id.hashCode().abs % 6, filled = true)),
        Node("div").children(
          Node("h4.title").text(scenarioResponse.entity.description),
          Node("div.tags")
            .child(Tag(s"Size: ${scenarioResponse.entity.initialRobots.size} players", "is-info"))
            .childOptional(if (scenarioResponse.entity.traps.nonEmpty) Some(Tag(s"Contains traps", "is-warning")) else None)
            .childOptional(
              if (context.global.sessionId == scenarioResponse.owner) Some(Tag(s"Created by you", "is-info")) else None
            ),
          ButtonList
            .left()
            .childOptional(
              if (context.global.sessionId == scenarioResponse.owner)
                Some(Button("Delete Scenario", SnabbdomEventListener.sideeffect(() => Actions.deleteScenario(scenarioResponse))))
              else
                None
            )
            .children(
              Button("Edit Scenario", SnabbdomEventListener.set(EditorState(scenarioResponse))),
              Button("Start Game", SnabbdomEventListener.set(PreviewState(scenarioResponse))).classes("is-primary")
            )
        )
      )
    ).classes("has-background-light")

}
