package roborace.frontend.game

//import com.raquo.snabbdom.simple.VNode
import roborace.frontend.{GameFrontendState, Service}
import roborace.frontend.util.SnabbdomApp
import gameEntities._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom.raw.Element

import scala.concurrent.Future
import scala.scalajs.js.|
//
//class Game(container: Element, game: GameResponse) extends SnabbdomApp {
//
//  var node: Element | VNode = container
//
//  val eventSource = Service.gameUpdates(game.id)
//
//  def renderState(state: GameState): Unit = {
//    eventSource.onmessage = message => {
//      val newGame = decode[GameResponse](message.data.asInstanceOf[String]).right.get
//      renderState(Game.newCycleEffects(state, state.copy(game = newGame)))
//    }
//
//    node = patch(node, GameUi(state, renderState))
//  }
//
//
//  renderState(GameState(
//    game = game,
//    focusedSlot = 0,
//    slots = Map.empty
//  ))
//}

object Game {
  def newCycleEffects(oldState: GameFrontendState, newState: GameFrontendState): GameFrontendState =
    if (oldState.game.cycle != newState.game.cycle) {
      oldState.copy(focusedSlot = 0, slots = Map.empty, game = newState.game)
    } else {
      oldState.copy(game = newState.game)
    }
}
