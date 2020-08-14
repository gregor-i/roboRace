package roborace.frontend.pages
package game

import api.{GameResponse, User}
import entities.Instruction
import monocle.macros.Lenses
import roborace.frontend.FrontendState
import roborace.frontend.Router.{Path, QueryParameter}
import roborace.frontend.service.Service
import snabbdom.Node

import scala.concurrent.ExecutionContext.Implicits.global

@Lenses
case class GameState(game: GameResponse, focusedSlot: Int = 0, slots: Map[Int, Instruction] = Map.empty) extends FrontendState

object GameState {
  def clearSlots(oldState: GameState, newState: GameState): GameState =
    if (oldState.game.cycle != newState.game.cycle) {
      oldState.copy(focusedSlot = 0, slots = Map.empty, game = newState.game)
    } else {
      oldState.copy(game = newState.game)
    }
}

object GamePage extends Page[GameState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = {
    case (_, s"/games/${gameId}", _) =>
      LoadingState {
        for {
          games <- Service.getAllGames()
          game = games.find(_.id == gameId)
          state = game match {
            case Some(game) => GameState(game)
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
