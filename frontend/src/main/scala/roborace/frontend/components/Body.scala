package roborace.frontend.components

import snabbdom.Node

object Body {
  def apply(): Node =
    Node("div.robo-race").prop("id", "robo-race")

  def game(): Node =
  val body = Node("div.game").prop("id", "robo-race")

}
