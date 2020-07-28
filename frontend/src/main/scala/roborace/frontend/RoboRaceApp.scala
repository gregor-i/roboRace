package roborace.frontend

import org.scalajs.dom
import org.scalajs.dom.Element
import roborace.frontend.error.ErrorState
import roborace.frontend.loading.LoadingFrontendState
import roborace.frontend.util.SnabbdomApp
import snabbdom.VNode

import scala.scalajs.js.|
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

class RoboRaceApp(container: Element) extends SnabbdomApp {

  var node: Element | VNode = container

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

    state match {
      case LoadingFrontendState(future, _) =>
        future.onComplete {
          case Success(newState) => renderState(newState)
          case Failure(exception) =>
            renderState(
              ErrorState(s"unexpected problem while initializing app: ${exception.getMessage}")
            )
        }
      case _ => ()
    }

    node = patch(node, Pages.ui(state, renderState).toVNode)
  }

  dom.window.onpopstate = _ => renderState(Router.stateFromUrl(dom.window.location, None)) // todo: get user

  renderState(Router.stateFromUrl(dom.window.location, None))
}
