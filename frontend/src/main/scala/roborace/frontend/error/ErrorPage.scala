package roborace.frontend
package error

import monocle.macros.Lenses
import roborace.frontend.Router.Location
import roborace.frontend.components.Body
import snabbdom._

//@Lenses
case class ErrorState(message: String, navbarExpanded: Boolean = false) extends FrontendState

object ErrorPage extends Page[ErrorState] {
  def stateFromUrl = PartialFunction.empty

  def stateToUrl(state: State): Option[Location] = None

  def render(implicit state: ErrorState, update: FrontendState => Unit): Node =
    Body()
    //      .child(Header(ErrorState.navbarExpanded))
      .child(
        Node("div.section")
          .child(
            Node("article.message.is-danger")
              .child(
                Node("div.message-body")
                  .child(Node("div.title").text("An unexpected error occured."))
                  .child(Node("div.subtitle").text(state.message))
                  .child(Node("a").attr("href", "/").text("return to landing page"))
              )
          )
      )
}
