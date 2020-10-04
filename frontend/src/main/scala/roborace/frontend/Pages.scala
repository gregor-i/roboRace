package roborace.frontend

import roborace.frontend.pages._
import roborace.frontend.pages.editor.EditorPage
import snabbdom.Node

object Pages {
  val all: Seq[Page[_ <: PageState]] = Seq(
    // global:
    GreetingPage,
    ErrorPage,
    LoadingPage,
    // tools:
    EditorPage,
    // multiplayer:
    multiplayer.lobby.LobbyPage,
    multiplayer.preview.PreviewPage,
    multiplayer.game.GamePage,
    // singleplayer:
    singleplayer.SelectLevelPage,
    singleplayer.GamePage
  )

  def selectPage[S <: PageState](nutriaState: S): Page[S] =
    all
      .find(_.acceptState(nutriaState))
      .map(_.asInstanceOf[Page[S]])
      .getOrElse(throw new Exception(s"No Page defined for '${nutriaState.getClass.getSimpleName}'"))

  def ui(context: Context[PageState]): Node =
    selectPage(context.local).render(context)
}
