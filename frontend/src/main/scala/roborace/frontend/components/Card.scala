package roborace.frontend.components

import org.scalajs.dom.MouseEvent
import snabbdom.Node

object Card {
  def apply(content: Node, actions: (String, Option[MouseEvent => Unit])*): Node =
    Node("div.card")
      .style("margin", "8px")
      .child(
        Node("div.card-content").child(content)
      )
      .childOptional {
        val actionNodes = actions.map {
          case (name, Some(action)) => Node("a.card-footer-item").text(name).event("click", action)
          case (name, None)         => Node("a.card-footer-item").text(name)
        }

        if (actionNodes.isEmpty)
          None
        else
          Some(Node("footer.card-footer").child(actionNodes))
      }
}
