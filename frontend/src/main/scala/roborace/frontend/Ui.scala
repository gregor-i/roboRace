package roborace.frontend

import snabbdom.Node

object Ui {
  def apply(state: State, update: State => Unit): Node =
    state match {
    case _: EditorState => ???
    case _: GameState => ???
    case _: LobbyState => ???
    case _: PreviewState => ???
    }
}
