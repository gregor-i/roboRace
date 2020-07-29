package roborace.frontend.lobby

import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.loading.LoadingFrontendState
import roborace.frontend.service.Service
import roborace.frontend.{FrontendState, LobbyFrontendState, Page, User, service}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object LobbyPage extends Page[LobbyFrontendState] {
  def load(): FrontendState = LoadingFrontendState(
    for {
      games     <- Service.getAllGames()
      scenarios <- service.Service.getAllScenarios()
    } yield LobbyFrontendState(games, scenarios)
  )

  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (user, "/", _) => load()
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some("/" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node = LobbyUi.render(state, update)
}
