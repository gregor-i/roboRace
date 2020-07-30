package roborace.frontend.components

import snabbdom.Node

object Tag {
  def apply(text: String, classes: String*): Node =
    Node("span.tag").classes(classes: _*).text(text)
}
