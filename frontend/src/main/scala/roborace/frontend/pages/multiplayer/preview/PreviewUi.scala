package roborace.frontend.pages.multiplayer.preview

import entities.{Direction, Position, Scenario}
import roborace.frontend.FrontendState
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Fab, Icons}
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.service.{Actions, Service}
import snabbdom.{Node, Snabbdom}

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewUi {
  def render(implicit state: PreviewState, update: FrontendState => Unit): Node =
    Body
      .game()
      .child(Fab(Icons.close).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load()))))
      .child(RenderScenario(state.scenario.scenario, Some(createGame(state.scenario.scenario, update))))
      .child(bottomLine)

  private def createGame(scenario: Scenario, update: FrontendState => Unit)(pos: Position, dir: Direction): Unit =
    scenario.initialRobots
      .find(_.position == pos)
      .foreach { robot =>
        (for {
          game <- Actions.createGame(scenario, robot.index)
        } yield GameState(game))
          .foreach(update)
      }

  def bottomLine: Node =
    Node("div.text-panel").text("To start this scenario, select a start position.")
}
