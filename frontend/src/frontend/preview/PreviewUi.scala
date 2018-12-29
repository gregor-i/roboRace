package frontend.preview

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.attrs.id
import com.raquo.snabbdom.simple.events.onClick
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.className
import com.raquo.snabbdom.simple.tags._
import frontend.components.{Fab, Images}
import frontend.gameBoard.RenderScenario
import frontend.util.Ui
import frontend.{Main, Service}
import gameEntities.{Direction, Position, Scenario}

import scala.concurrent.ExecutionContext.Implicits.global

object PreviewUi extends Ui {
  def render(previewState: PreviewState): VNode =
    div(id := "robo-race",
      className := "game",
      Fab("fab-right-1", Images.iconClose, onClick := (() => Main.gotoLobby())),
      RenderScenario(previewState.scenario.scenario, Some(createGame(previewState.scenario.scenario))),
      bottomLine
    )

  private def createGame(scenario: Scenario)(pos: Position, dir: Direction): Unit =
    scenario.initialRobots.find(_.position == pos)
      .foreach(robot => Service.createGame(scenario, robot.index)
        .foreach(Main.gotoGame))

  def bottomLine: VNode =
    div(className := "text-panel", "To start this scenario, select a start position.")
}
