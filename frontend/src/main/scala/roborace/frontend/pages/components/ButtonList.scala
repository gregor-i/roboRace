package roborace.frontend.pages.components

import snabbdom.Node

object ButtonList {
  def apply(buttons: Node*): Node = Node("div.buttons.is-right").child(buttons)

  def fullWidth(buttons: Node*): Node =
    Node("div.buttons.is-right")
      .style("display", "flex")
      .child(buttons.map(_.style("flexGrow", "1")))
}
