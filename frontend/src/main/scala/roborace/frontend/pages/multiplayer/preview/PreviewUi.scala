package roborace.frontend.pages
package multiplayer.preview

import entities.{Direction, Position, Scenario}
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Fab, Icons}
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.pages.multiplayer.preview.PreviewPage.Context
import roborace.frontend.service.Actions
import roborace.frontend.util.SnabbdomEventListener
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewUi {
  def render(implicit context: Context): Node =
    Body
      .game()
      .child(Fab(Icons.close).classes("fab-right-1").event("click", SnabbdomEventListener.set(LobbyPage.load())))
      .child(RenderScenario(context.local.scenario.scenario, Some(createGame(context.local.scenario.scenario))))
      .child(bottomLine)

  private def createGame(scenario: Scenario)(pos: Position, dir: Direction)(implicit context: Context): Unit =
    scenario.initialRobots
      .find(_.position == pos)
      .foreach { robot =>
        (for {
          game <- Actions.createGame(scenario, robot.index)
        } yield GameState(game))
          .foreach(context.update)
      }

  def bottomLine: Node =
    Node("div.text-panel").text("To start this scenario, select a start position.")
}
