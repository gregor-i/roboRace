package roborace.frontend.editor

import gameLogic.DefaultScenario
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.{EditorState, FrontendState, Page, User}
import snabbdom.Node

object EditorPage extends Page[EditorState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/editor", _) => EditorState(DefaultScenario.default)
  }

  override def stateToUrl(state: EditorState): Option[(Path, QueryParameter)] =
    Some("/editor" -> Map.empty)

  override def render(implicit state: EditorState, update: FrontendState => Unit): Node =
    EditorUi(state, update)
}
