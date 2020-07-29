package roborace.frontend.gameBoard

import gameEntities._
import snabbdom.Node

object RenderScenario {
  def apply(scenario: Scenario, click: Option[(Position, Direction) => Unit]): Node = {
    Node("div.game-board")
      .child(
        Node("svg")
          .attr("viewBox", s"0 0 ${Svg.width(scenario)} ${Svg.height(scenario)}")
          .children(
            group("tiles", Svg.tiles(scenario, click)),
            group("walls", Svg.walls(scenario)),
            group("targets", Svg.targets(scenario, None)),
            group("traps", Svg.traps(scenario)),
            group("startPoints", Svg.startPoints(scenario))
          )
      )
  }

  private def group(name: String, nodes: Seq[Node]): Node =
    Node("g").attr("name", name).child(nodes)
}
