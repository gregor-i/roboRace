package roborace.frontend.preview

import gameEntities.{Direction, Position, Scenario}
import roborace.frontend.FrontendState
import roborace.frontend.components.{Body, Fab, Icon, Images}
import roborace.frontend.game.GameState
import roborace.frontend.components.gameBoard.RenderScenario
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.service.Service
import snabbdom.{Node, Snabbdom}

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewUi {
  def render(state: PreviewState, update: FrontendState => Unit): Node =
    Body
      .game()
      .child(Fab(Icon.close).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load()))))
      .child(RenderScenario(state.scenario.scenario, Some(createGame(state.scenario.scenario, update))))
      .child(bottomLine)

  private def createGame(scenario: Scenario, update: FrontendState => Unit)(pos: Position, dir: Direction): Unit =
    scenario.initialRobots
      .find(_.position == pos)
      .foreach { robot =>
        (for {
          game <- Service.createGame(scenario, robot.index)
        } yield GameState(game))
          .foreach(update)
      }

  def bottomLine: Node =
    Node("div.text-panel").text("To start this scenario, select a start position.")
}
