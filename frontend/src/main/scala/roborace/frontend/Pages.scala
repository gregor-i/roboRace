package roborace.frontend

import roborace.frontend.error.ErrorPage
import roborace.frontend.loading.LoadingPage
import roborace.frontend.lobby.LobbyPage
import snabbdom.Node

object Pages {
  val all: Seq[Page[_ <: FrontendState]] = Seq(
    LobbyPage,
    ErrorPage,
    LoadingPage
  )

  def selectPage[S <: FrontendState](nutriaState: S): Page[S] =
    all
      .find(_.acceptState(nutriaState))
      .map(_.asInstanceOf[Page[S]])
      .getOrElse(throw new Exception(s"No Page defined for '${nutriaState.getClass.getSimpleName}'"))

  def ui(nutriaState: FrontendState, update: FrontendState => Unit): Node =
    selectPage(nutriaState).render(nutriaState, update)
}
