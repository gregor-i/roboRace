package roborace.frontend.pages

import roborace.frontend.Router.Location
import roborace.frontend.components.Body
import roborace.frontend.{FrontendState, Page}
import snabbdom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class LoadingState(loading: Future[FrontendState], navbarExpanded: Boolean = false) extends FrontendState

object LoadingPage extends Page[LoadingState] {
  def stateFromUrl = PartialFunction.empty

  def stateToUrl(state: State): Option[Location] = None

  def render(implicit state: LoadingState, update: FrontendState => Unit) =
    Body()
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
                ErrorState(s"unexpected problem while initializing app: ${exception.getMessage}")
              )
          }
        }
      )
}
