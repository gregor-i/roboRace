package roborace.frontend.pages.components

import roborace.frontend.{Context, PageState, Router}
import snabbdom.Node

object Body {
  def apply()(implicit context: Context[PageState]): Node =
    Node("div.robo-race")
      .prop("id", "robo-race")
      .key(Router.stateToUrl(context.local).fold("")(_._1))
      .classes(context.local.getClass.getSimpleName)

  def game()(implicit context: Context[PageState]): Node =
    Node("div.game")
      .prop("id", "robo-race")
      .key(Router.stateToUrl(context.local).fold("")(_._1))
      .classes(context.local.getClass.getSimpleName)

}
