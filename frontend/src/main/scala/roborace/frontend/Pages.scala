package roborace.frontend

import roborace.frontend.error.ErrorPage
import roborace.frontend.game.GamePage
import roborace.frontend.loading.LoadingPage
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.preview.PreviewPage
import snabbdom.Node

object Pages {
  val all: Seq[Page[_ <: FrontendState]] = Seq(
    PreviewPage,
    LobbyPage,
    ErrorPage,
    LoadingPage,
    GamePage
  )

  def selectPage[S <: FrontendState](nutriaState: S): Page[S] =
    all
      .find(_.acceptState(nutriaState))
      .map(_.asInstanceOf[Page[S]])
      .getOrElse(throw new Exception(s"No Page defined for '${nutriaState.getClass.getSimpleName}'"))

  def ui(nutriaState: FrontendState, update: FrontendState => Unit): Node =
    selectPage(nutriaState).render(nutriaState, update)
}
