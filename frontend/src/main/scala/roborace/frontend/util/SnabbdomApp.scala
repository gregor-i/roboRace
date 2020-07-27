package roborace.frontend.util

import snabbdom.{Snabbdom, SnabbdomFacade}

trait SnabbdomApp {
  val patch: SnabbdomFacade.PatchFunction = Snabbdom.init()
}

object SnabbdomApp
