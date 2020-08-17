package roborace.frontend

import roborace.frontend.pages._
import roborace.frontend.pages.editor.EditorPage
import roborace.frontend.pages.multiplayer.game.GamePage
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.pages.multiplayer.preview.PreviewPage
import roborace.frontend.pages.singleplayer.SelectLevelPage
import snabbdom.Node

object Pages {
  val all: Seq[Page[_ <: FrontendState]] = Seq(
    // global:
    GreetingPage,
    ErrorPage,
    LoadingPage,
    // tools:
    EditorPage,
    // multiplayer:
    LobbyPage,
    PreviewPage,
    GamePage,
    // singleplayer:
    SelectLevelPage
  )

  def selectPage[S <: FrontendState](nutriaState: S): Page[S] =
    all
      .find(_.acceptState(nutriaState))
      .map(_.asInstanceOf[Page[S]])
      .getOrElse(throw new Exception(s"No Page defined for '${nutriaState.getClass.getSimpleName}'"))

  def ui(nutriaState: FrontendState, update: FrontendState => Unit): Node =
    selectPage(nutriaState).render(nutriaState, update)
}
