package roborace.frontend.pages
package singleplayer

import entities.{Levels, Scenario}
import roborace.frontend.pages.components._
import roborace.frontend.util.SnabbdomEventListener
import roborace.frontend.{GlobalState, PageState}
import snabbdom.Node

case class SelectLevelState() extends PageState

object SelectLevelPage extends Page[SelectLevelState] {
  override def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState] = {
    case (_, "/singleplayer", _) => SelectLevelState()
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] =
    Some("/singleplayer" -> Map.empty)

  override def render(implicit context: Context): Node =
    Body()
      .child(Header())
      .child(
        Column(
          Levels.all.map(levelCard(_))
        )
      )

  private def levelCard(scenario: Scenario)(implicit context: Context) =
    Card(
      MediaObject(
        Some(RobotImage.apply(1, filled = context.global.finishedSinglePlayerLevels.contains(scenario.hashCode().toHexString))),
        Node("button.button.is-primary")
          .text("Start Game")
          .event("click", SnabbdomEventListener.set(GameState.start(scenario)))
      )
    ).classes("has-background-light")
}
