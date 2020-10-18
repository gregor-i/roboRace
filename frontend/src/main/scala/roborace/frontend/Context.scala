package roborace.frontend

import api.User
import monocle.macros.Lenses
import roborace.frontend.util.Updatable

@Lenses
case class GlobalState(
    user: Option[User] = None, // todo: placeholder value
    navbarExpanded: Boolean = false,
    finishedSinglePlayerLevels: Set[String] = Set.empty
)

object GlobalState {
  val initial: GlobalState = GlobalState()
}

trait PageState

trait Context[+S <: PageState] {
  def local: S
  def update(pageState: PageState): Unit

  def global: GlobalState
  def update(globalState: GlobalState): Unit

  def update(globalState: GlobalState, pageState: PageState): Unit

  // todo: try to get rid of it
  def localUpdatable: Updatable[S, PageState] = Updatable[S, PageState](local, update)
}

object Context {
  def apply(pageState: PageState, globalState: GlobalState, renderState: (GlobalState, PageState) => Unit): Context[PageState] =
    new Context[PageState] {
      def local: PageState                  = pageState
      def update(newState: PageState): Unit = renderState(globalState, newState)

      def global: GlobalState                 = globalState
      def update(newState: GlobalState): Unit = renderState(newState, pageState)

      def update(newGlobalState: GlobalState, newPageState: PageState): Unit = renderState(newGlobalState, newPageState)
    }
}
