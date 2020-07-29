package roborace.frontend.preview

import gameEntities.{Direction, Position, Scenario}
import roborace.frontend.components.{Fab, Images}
import roborace.frontend.gameBoard.RenderScenario
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.service.Service
import roborace.frontend.{FrontendState, GameFrontendState, PreviewFrontendState}
import snabbdom.{Node, Snabbdom}

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewUi {
  def render(state: PreviewFrontendState, update: FrontendState => Unit): Node =
    Node("div.game")
      .prop("id", "robo-race")
      .child(Fab(Images.iconClose).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load()))))
      .child(RenderScenario(state.scenario.scenario, Some(createGame(state.scenario.scenario, update))))
      .child(bottomLine)

  private def createGame(scenario: Scenario, update: FrontendState => Unit)(pos: Position, dir: Direction): Unit =
    scenario.initialRobots
      .find(_.position == pos)
      .foreach { robot =>
        (for {
          game <- Service.createGame(scenario, robot.index)
        } yield GameFrontendState(game))
          .foreach(update)
      }

  def bottomLine: Node =
    Node("div.text-panel").text("To start this scenario, select a start position.")
}
