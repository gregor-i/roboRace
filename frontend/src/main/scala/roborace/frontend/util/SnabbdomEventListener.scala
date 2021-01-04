package roborace.frontend.util

import roborace.frontend.{Context, PageState}
import snabbdom.Event

object SnabbdomEventListener {
  def modify[S <: PageState](modify: S => S)(implicit context: Context[S]): Event => Unit =
    _ => context.update(modify(context.local))

  def set(newState: => PageState)(implicit context: Context[_]): Event => Unit =
    _ => context.update(newState)

  def sideeffect(operation: () => Unit): Event => Unit =
    _ => operation()

  val noop: Event => Unit =
    _ => ()
}
