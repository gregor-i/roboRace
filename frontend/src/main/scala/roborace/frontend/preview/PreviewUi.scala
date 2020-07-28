package roborace.frontend.preview

import roborace.frontend.components.{Fab, Images}
import roborace.frontend.gameBoard.RenderScenario
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.{FrontendState, PreviewFrontendState}
import snabbdom.{Node, Snabbdom}

object PreviewUi {
  def render(state: PreviewFrontendState, update: FrontendState => Unit): Node =
    Node("div.game")
      .prop("id", "robo-race")
      .child(Fab(Images.iconClose).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load()))))
      .child(RenderScenario(state.scenario.scenario, None /*Some(createGame(previewState.scenario.scenario)*/ ))
      .child(bottomLine)

//  private def createGame(scenario: Scenario)(pos: Position, dir: Direction): Unit =
//    scenario.initialRobots
//      .find(_.position == pos)
//      .foreach(
//        robot =>
//          Service
//            .createGame(scenario, robot.index)
//            .foreach(Main.gotoGame)
//      )

  def bottomLine: Node =
    Node("div.text-panel").text("To start this scenario, select a start position.")
}
