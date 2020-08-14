package roborace.frontend.pages.components

import snabbdom.Node

object Column {
  def apply(nodes: Seq[Node]): Node =
    Node("div.columns.is-centered.is-mobile")
      .child(
        Node("div.column.single-column").child(nodes)
      )
}
