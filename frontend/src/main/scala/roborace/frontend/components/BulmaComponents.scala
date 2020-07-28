package roborace.frontend.components

import org.scalajs.dom.MouseEvent
import snabbdom.Node

object BulmaComponents {
  def singleColumn(mods: Seq[Node]): Node =
    Node("div.columns.is-centered.is-mobile")
      .child(
        Node("div.column.single-column").child(mods)
      )

  def card(content: Node, actions: (String, Option[MouseEvent => Unit])*): Node =
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

  def mediaObject(image: Option[Node], content: Node): Node =
    Node("div.media")
      .childOptional(
        image.map(
          l =>
            Node("figure.media-left")
              .child(
                Node("p.image")
                  .attr("width", "64px")
                  .attr("height", "64px")
                  .child(l)
              )
        )
      )
      .child(
        Node("div.media-content").child(content)
      )

  def tag(text: String, classes: String*): Node =
    Node("span.tag").classes(classes: _*).text(text)

}
