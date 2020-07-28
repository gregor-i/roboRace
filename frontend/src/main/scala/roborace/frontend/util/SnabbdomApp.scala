package roborace.frontend.util

import snabbdom.{Snabbdom, SnabbdomFacade}

trait SnabbdomApp {
  val patch: SnabbdomFacade.PatchFunction = Snabbdom.init(
    classModule = true,
    propsModule = true,
    attributesModule = true,
    datasetModule = true,
    styleModule = true,
    eventlistenersModule = true
  )
}

object SnabbdomApp
