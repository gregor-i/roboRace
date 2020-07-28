package roborace.frontend.components

import snabbdom.Node

object Fab {
  def apply(image: String): Node =
    Node("div.fab")
      .child(Node("img").attr("src", image))
}
