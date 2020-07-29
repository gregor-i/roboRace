package roborace.frontend.game

import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.error.ErrorState
import roborace.frontend.loading.LoadingFrontendState
import roborace.frontend.service.Service
import roborace.frontend.{FrontendState, GameFrontendState, Page, User}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

object GamePage extends Page[GameFrontendState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, s"/games/${gameId}", _) =>
      LoadingFrontendState {
        for {
          games <- Service.getAllGames()
          game = games.find(_.id == gameId)
          state = game match {
            case Some(game) => GameFrontendState(game)
            case None       => ErrorState("Game not found")
          }
        } yield state
      }
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] =
    Some(s"/games/${state.game.id}" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node =
    GameUi(state, update)
}
