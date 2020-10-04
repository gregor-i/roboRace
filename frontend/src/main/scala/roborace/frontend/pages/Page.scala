package roborace.frontend.pages

import roborace.frontend.{GlobalState, PageState}
import snabbdom.Node

import scala.reflect.ClassTag

abstract class Page[S <: PageState: ClassTag] {
  type State   = S
  type Context = roborace.frontend.Context[S]
  type Update  = PageState => Unit

  def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState]

  def stateToUrl(state: State): Option[Location]

  def render(implicit context: Context): Node

  def acceptState(nutriaState: PageState): Boolean = implicitly[ClassTag[State]].runtimeClass == nutriaState.getClass
}
