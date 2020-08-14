package roborace.frontend.pages.components

import snabbdom.Node

object Fab {
  def apply(fontAwesomeClass: String): Node =
    Node("div.fab.fa-lg").child(Node("i.fas").classes(fontAwesomeClass))
}
