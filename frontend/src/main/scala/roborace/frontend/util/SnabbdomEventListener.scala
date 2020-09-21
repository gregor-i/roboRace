package roborace.frontend.util

import snabbdom.{Snabbdom, SnabbdomFacade}

object SnabbdomEventListener {
  def modify[S](modify: S => S)(implicit state: S, update: S => Unit): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => update(modify(state)))

  def set[S](newState: => S)(implicit update: S => Unit): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => update(newState))

  def sideeffect(operation: () => Unit): SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => operation())

  val noop: SnabbdomFacade.Eventlistener =
    Snabbdom.event(_ => ())
}
