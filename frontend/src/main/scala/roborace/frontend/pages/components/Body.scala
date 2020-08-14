package roborace.frontend.pages.components

import roborace.frontend.{FrontendState, Router}
import snabbdom.Node

object Body {
  def apply()(implicit state: FrontendState): Node =
    Node("div.robo-race")
      .prop("id", "robo-race")
      .key(Router.stateToUrl(state).fold("")(_._1))
      .classes(state.getClass.getSimpleName)

  def game()(implicit state: FrontendState): Node =
    Node("div.game")
      .prop("id", "robo-race")
      .key(Router.stateToUrl(state).fold("")(_._1))
      .classes(state.getClass.getSimpleName)

}
