package roborace.frontend

import roborace.frontend.pages.{ErrorPage, LoadingPage}
import roborace.frontend.pages.editor.EditorPage
import roborace.frontend.pages.game.GamePage
import roborace.frontend.pages.lobby.LobbyPage
import roborace.frontend.pages.preview.PreviewPage
import snabbdom.Node

object Pages {
  val all: Seq[Page[_ <: FrontendState]] = Seq(
    PreviewPage,
    LobbyPage,
    ErrorPage,
    LoadingPage,
    GamePage,
    EditorPage
  )

  def selectPage[S <: FrontendState](nutriaState: S): Page[S] =
    all
      .find(_.acceptState(nutriaState))
      .map(_.asInstanceOf[Page[S]])
      .getOrElse(throw new Exception(s"No Page defined for '${nutriaState.getClass.getSimpleName}'"))

  def ui(nutriaState: FrontendState, update: FrontendState => Unit): Node =
    selectPage(nutriaState).render(nutriaState, update)
}
