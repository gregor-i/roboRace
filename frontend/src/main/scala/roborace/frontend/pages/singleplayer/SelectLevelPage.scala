package roborace.frontend.pages
package singleplayer

import api.User
import roborace.frontend.FrontendState
import roborace.frontend.pages.components._
import snabbdom.Node

object SelectLevelState extends FrontendState

object SelectLevelPage extends Page[SelectLevelState.type] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/singleplayer", _) => SelectLevelState
  }
  override def stateToUrl(state: State): Option[(Path, QueryParameter)] =
    Some("/singleplayer" -> Map.empty)

  override def render(implicit state: State, update: Update): Node =
    Body()
      .child(Header())
      .child(
        Column(
          Seq(levels())
        )
      )

  private def levels() =
    Card(
      MediaObject(
        Some(RobotImage.apply(1, filled = true)),
        Node("div").text("todo")
      )
    ).classes("has-background-light")
}
