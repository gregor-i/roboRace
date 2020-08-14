package roborace.frontend.pages.components

import snabbdom.Node
import snabbdom.SnabbdomFacade.Eventlistener

object Modal {
  def apply(closeAction: Eventlistener, background: Option[Node] = None)(content: Node*): Node =
    Node("div.modal.is-active")
      .child(
        Node("div.modal-background")
          .event("click", closeAction)
          .style("display", "flex")
          .childOptional(background)
      )
      .child(Node("div.modal-content").child(Node("div.box").child(content)))
}
