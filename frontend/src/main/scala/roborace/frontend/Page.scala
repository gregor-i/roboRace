package roborace.frontend

import snabbdom.Node

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

abstract class Page[S <: FrontendState: ClassTag] {
  type State  = S
  type Update = State => Unit

  def stateFromUrl: PartialFunction[(Option[User], Router.Path, Router.QueryParameter), FrontendState]

  def stateToUrl(state: State): Option[Router.Location]

  def render(implicit state: State, update: FrontendState => Unit): Node

  def acceptState(nutriaState: FrontendState): Boolean = implicitly[ClassTag[State]].runtimeClass == nutriaState.getClass
}
