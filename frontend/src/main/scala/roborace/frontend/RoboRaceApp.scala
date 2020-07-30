package roborace.frontend

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import roborace.frontend.util.SnabbdomApp
import snabbdom.VNode

import scala.scalajs.js.|

//  def renderState(state: GameState): Unit = {
//    eventSource.onmessage = message => {
//      val newGame = decode[GameResponse](message.data.asInstanceOf[String]).right.get
//      renderState(Game.newCycleEffects(state, state.copy(game = newGame)))
//    }
//
//    node = patch(node, GameUi(state, renderState))
//  }

class RoboRaceApp(container: HTMLElement) extends SnabbdomApp {
  val user: User = User(sessionId = container.dataset.get("sessionId").get)

  var node: HTMLElement | VNode = container

  def renderState(state: FrontendState): Unit = {
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

    node = patch(node, Pages.ui(state, renderState).toVNode)
  }

  dom.window.onpopstate = _ => renderState(Router.stateFromUrl(dom.window.location, Some(user)))

  renderState(Router.stateFromUrl(dom.window.location, Some(user)))
}
