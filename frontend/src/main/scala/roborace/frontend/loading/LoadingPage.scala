package roborace.frontend
package loading

import monocle.macros.Lenses
import roborace.frontend.Router.Location
import roborace.frontend.error.ErrorState
import snabbdom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

//@Lenses
case class LoadingFrontendState(user: Option[User], loading: Future[FrontendState], navbarExpanded: Boolean = false) extends FrontendState

object LoadingPage extends Page[LoadingFrontendState] {
  def stateFromUrl = PartialFunction.empty

  def stateToUrl(state: State): Option[Location] = None

  def render(implicit state: LoadingFrontendState, update: FrontendState => Unit) =
    Node("div")
      .child(
        Node("i.fa.fa-spinner.fa-pulse.has-text-primary")
          .styles(
            Seq(
              "position"   -> "absolute",
              "left"       -> "50%",
              "top"        -> "50%",
              "marginLeft" -> "-5rem",
              "fontSize"   -> "10rem"
            )
          )
      )
      .hook(
        "insert",
        Snabbdom.hook { _ =>
          state.loading.onComplete {
            case Success(newState) => update(newState)
            case Failure(exception) =>
              update(
                ErrorState(state.user, s"unexpected problem while initializing app: ${exception.getMessage}")
              )
          }
        }
      )
}
