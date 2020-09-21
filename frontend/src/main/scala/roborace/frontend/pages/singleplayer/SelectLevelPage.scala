package roborace.frontend.pages
package singleplayer

import api.User
import entities.{Levels, Scenario}
import roborace.frontend.FrontendState
import roborace.frontend.pages.components._
import snabbdom.{Node, Snabbdom}

case class SelectLevelState() extends FrontendState

object SelectLevelPage extends Page[SelectLevelState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/singleplayer", _) => SelectLevelState()
  }
  override def stateToUrl(state: State): Option[(Path, QueryParameter)] =
    Some("/singleplayer" -> Map.empty)

  override def render(implicit state: State, update: Update): Node =
    Body()
      .child(Header())
      .child(
        Column(
          Levels.all.map(levelCard(_))
        )
      )

  private def levelCard(scenario: Scenario)(implicit update: Update) =
    Card(
      MediaObject(
        Some(RobotImage.apply(1, filled = true)),
        Node("button.button.is-primary")
          .text("Start Game")
          .event("click", Snabbdom.event(_ => update(SinglePlayerGameState.start(scenario))))
      )
    ).classes("has-background-light")
}
