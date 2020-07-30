package roborace.frontend.editor

import gameEntities.Scenario
import gameLogic.DefaultScenario
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.{FrontendState, Page, User, editor}
import snabbdom.Node

case class EditorState(scenario: Scenario, description: String = "", clickAction: Option[editor.ClickAction] = None) extends FrontendState

object EditorPage extends Page[EditorState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/editor", _) => EditorState(DefaultScenario.default)
  }

  override def stateToUrl(state: EditorState): Option[(Path, QueryParameter)] =
    Some("/editor" -> Map.empty)

  override def render(implicit state: EditorState, update: FrontendState => Unit): Node =
    EditorUi(state, update)
}
