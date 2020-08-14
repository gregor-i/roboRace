package roborace.frontend.pages
package editor

import api.User
import gameEntities.Scenario
import gameLogic.DefaultScenario
import monocle.macros.Lenses
import roborace.frontend.FrontendState
import roborace.frontend.Router.{Path, QueryParameter}
import snabbdom.Node

@Lenses
case class EditorState(scenario: Scenario, description: String = "", clickAction: Option[ClickAction] = None) extends FrontendState

object EditorPage extends Page[EditorState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/editor", _) => EditorState(DefaultScenario.default)
  }

  override def stateToUrl(state: EditorState): Option[(Path, QueryParameter)] =
    Some("/editor" -> Map.empty)

  override def render(implicit state: EditorState, update: FrontendState => Unit): Node =
    EditorUi(state, update)
}
