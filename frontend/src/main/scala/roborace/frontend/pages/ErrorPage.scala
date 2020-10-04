package roborace.frontend.pages

import roborace.frontend.PageState
import roborace.frontend.pages.components.{Body, Header}
import snabbdom._

case class ErrorState(message: String, navbarExpanded: Boolean = false) extends PageState

object ErrorPage extends Page[ErrorState] {
  def stateFromUrl = PartialFunction.empty

  def stateToUrl(state: State): Option[Location] = None

  override def render(implicit context: Context): Node =
    Body()
      .child(Header())
      .child(
        Node("div.section")
          .child(
            Node("article.message.is-danger")
              .child(
                Node("div.message-body")
                  .child(Node("div.title").text("An unexpected error occured."))
                  .child(Node("div.subtitle").text(context.local.message))
                  .child(Node("a").attr("href", "/").text("return to landing page"))
              )
          )
      )
}
