package roborace.frontend.lobby

import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.loading.LoadingFrontendState
import roborace.frontend.{FrontendState, LobbyFrontendState, Page, User}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object LobbyPage extends Page[LobbyFrontendState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (user, "/", _) =>
      LoadingFrontendState.apply(
        user,
        for {
          games     <- roborace.frontend.Service.getAllGames()
          scenarios <- roborace.frontend.Service.getAllScenarios()
        } yield LobbyFrontendState(games, scenarios)
      )
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some("/" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node = LobbyUi.render(state)
}
