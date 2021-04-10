package roborace.frontend.pages.components

import snabbdom.Node

object Icons {
  val close  = "fa-times"
  val replay = "fa-undo"
  val list   = "fa-list"

  def icon(icon: String): Node =
    Node("span.icon")
      .child(Node("i.fas").`class`(icon))
}
