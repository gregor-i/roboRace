package roborace.frontend.pages.components

import snabbdom.Node

object Column {
  def apply(): Node =
    Node("div")
      .style("maxWidth", "600px")
      .style("margin", "0 auto")
}
