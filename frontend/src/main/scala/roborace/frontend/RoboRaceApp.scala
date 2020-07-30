package roborace.frontend

import gameEntities.GameResponse
import io.circe.parser.decode
import org.scalajs.dom
import org.scalajs.dom.{EventSource, MessageEvent}
import org.scalajs.dom.raw.HTMLElement
import roborace.frontend.error.ErrorState
import roborace.frontend.game.{Game, GameState}
import roborace.frontend.service.Service
import roborace.frontend.util.SnabbdomApp
import snabbdom.VNode
import io.circe.generic.auto._

import scala.scalajs.js
import scala.scalajs.js.|

class RoboRaceApp(container: HTMLElement) extends SnabbdomApp {
  val user: User = User(sessionId = container.dataset.get("sessionId").get)

  var node: HTMLElement | VNode = container

  private def saveStateToHistory(state: FrontendState): Unit = {
    Router.stateToUrl(state) match {
      case Some((currentPath, currentSearch)) =>
        val stringSearch = Router.queryParamsToUrl(currentSearch)
        if (dom.window.location.pathname != currentPath) {
          dom.window.scroll(0, 0)
          dom.window.history.pushState(null, "", currentPath + stringSearch)
        } else {
          dom.window.history.replaceState(null, "", currentPath + stringSearch)
        }
      case None => ()
    }
  }

  var gameEventSource: Option[EventSource] = None
  def gameUpdates(state: FrontendState): Unit = {
    def eventListener(gameState: GameState): js.Function1[MessageEvent, Unit] = message => {
      decode[GameResponse](message.data.asInstanceOf[String]) match {
        case Right(newGame) => renderState(Game.clearSlots(gameState, gameState.copy(game = newGame)))
        case Left(_)        => renderState(ErrorState("unexpected Message received on SSE"))
      }
    }

    (state, gameEventSource) match {
      case (state: GameState, Some(eventSource)) =>
        eventSource.onmessage = eventListener(state)

      case (state: GameState, None) =>
        val eventSource = Service.gameUpdates(state.game.id)
        eventSource.onmessage = eventListener(state)
        gameEventSource = Some(eventSource)

      case (_, Some(eventSource)) =>
        eventSource.close()
        gameEventSource = None

      case (_, None) => ()
    }
  }

  def renderState(state: FrontendState): Unit = {
    saveStateToHistory(state)
    gameUpdates(state)

    node = patch(node, Pages.ui(state, renderState).toVNode)
  }

  dom.window.onpopstate = _ => renderState(Router.stateFromUrl(dom.window.location, Some(user)))

  renderState(Router.stateFromUrl(dom.window.location, Some(user)))
}
