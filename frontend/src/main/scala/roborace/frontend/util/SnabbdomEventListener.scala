package roborace.frontend.util

import roborace.frontend.{Context, PageState}
import snabbdom.{Snabbdom, SnabbdomFacade}

object SnabbdomEventListener {
  def modify[S <: PageState](modify: S => S)(implicit context: Context[S]): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => context.update(modify(context.local)))

  def set(newState: => PageState)(implicit context: Context[_]): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => context.update(newState))

  def sideeffect(operation: () => Unit): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => operation())

  val noop: SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => ())
}
