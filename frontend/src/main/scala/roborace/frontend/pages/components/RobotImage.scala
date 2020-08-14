package roborace.frontend.pages.components

import snabbdom.Node

object RobotImage {
  def apply(index: Int, filled: Boolean): Node =
    Node("img.robot-tile")
      .attr("src", (if (filled) Images.player(index) else Images.playerStart(index)))
}
