package roborace.frontend.pages

import roborace.frontend.PageState
import roborace.frontend.pages.components.Body
import snabbdom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class LoadingState(loading: Future[PageState], navbarExpanded: Boolean = false) extends PageState

object LoadingPage extends Page[LoadingState] {
  def stateFromUrl = PartialFunction.empty

  def stateToUrl(state: State): Option[Location] = None

  override def render(implicit context: Context) =
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
      .key(context.local.loading.hashCode())
      .hookInsert { _ =>
        context.local.loading.onComplete {
          case Success(newState) => context.update(newState)
          case Failure(exception) =>
            context.update(
              ErrorState(s"unexpected problem while initializing app: ${exception.getMessage}")
            )
        }
      }
}
