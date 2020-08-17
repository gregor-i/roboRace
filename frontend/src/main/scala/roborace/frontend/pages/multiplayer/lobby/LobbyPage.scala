package roborace.frontend.pages
package multiplayer.lobby

import api.{GameResponse, ScenarioResponse, User}
import monocle.macros.Lenses
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import roborace.frontend.{FrontendState, service}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses
case class LobbyState(games: Seq[GameResponse], scenarios: Seq[ScenarioResponse]) extends FrontendState

object LobbyPage extends Page[LobbyState] {
  def load(): FrontendState = LoadingState(
    for {
      games     <- Service.getAllGames()
      scenarios <- service.Service.getAllScenarios()
    } yield LobbyState(games, scenarios)
  )

  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, "/lobby", _) => load()
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some("/lobby" -> Map.empty)

  override def render(implicit state: State, update: FrontendState => Unit): Node = LobbyUi.render(state, update)
}
