package roborace.frontend.pages

import api.User
import snabbdom.Node

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

abstract class Page[S <: FrontendState: ClassTag] {
  type State  = S
  type Update = FrontendState => Unit

  def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState]

  def stateToUrl(state: State): Option[Location]

  def render(implicit state: State, update: Update): Node

  def acceptState(nutriaState: FrontendState): Boolean = implicitly[ClassTag[State]].runtimeClass == nutriaState.getClass
}
