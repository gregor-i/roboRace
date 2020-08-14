package roborace.frontend.pages.components

import snabbdom.Node

object RobotImage {
  def apply(index: Int, filled: Boolean): Node =
    Node("img")
      .attr("src", (if (filled) Images.player(index) else Images.playerStart(index)))
      .style("background", s"url(${Images.tile})")
      .style("backgroundSize", "contain")
}
