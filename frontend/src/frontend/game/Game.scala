package frontend.game

import com.raquo.snabbdom.simple.VNode
import frontend.Service
import frontend.util.SnabbdomApp
import gameEntities._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom.raw.Element

import scala.scalajs.js.|

class Game(container: Element, game: GameResponse) extends SnabbdomApp{

  var node: Element | VNode = container

  val eventSource = Service.gameUpdates(game.id)

  def renderState(state: GameState): Unit = {
    eventSource.onmessage = message => {
      val newGame = decode[GameResponse](message.data.asInstanceOf[String]).right.get
      val newState = if (state.game.cycle != newGame.cycle) {
        state.copy(focusedSlot = 0, game = newGame)
      }else{
        state.copy(game = newGame)
      }
      renderState(newState)
    }
    node = patch(node, GameUi(state, renderState))
  }

  renderState(GameState(game = game, focusedSlot = 0))
}
