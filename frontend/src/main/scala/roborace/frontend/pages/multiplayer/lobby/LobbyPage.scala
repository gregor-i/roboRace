package roborace.frontend.pages
package multiplayer.lobby

import api.{GameResponse, ScenarioResponse}
import monocle.macros.Lenses
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import roborace.frontend.{GlobalState, PageState, service}
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses
case class LobbyState(games: Seq[GameResponse], scenarios: Seq[ScenarioResponse]) extends PageState

object LobbyPage extends Page[LobbyState] {
  def load(): PageState = LoadingState(
    for {
      games     <- Service.getAllGames()
      scenarios <- service.Service.getAllScenarios()
    } yield LobbyState(games, scenarios)
  )

  override def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState] = {
    case (_, "/lobby", _) => load()
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = Some("/lobby" -> Map.empty)

  override def render(implicit context: Context): Node = LobbyUi.render(context)
}
