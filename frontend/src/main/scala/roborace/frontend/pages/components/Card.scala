package roborace.frontend.pages.components

import snabbdom.Node

object Card {
  def apply(content: Node): Node =
    Node("div.card")
      .style("margin", "8px")
      .child(
        Node("div.card-content").child(content)
      )
}
