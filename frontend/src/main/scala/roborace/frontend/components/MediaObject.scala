package roborace.frontend.components

import snabbdom.Node

object MediaObject {
  def apply(image: Option[Node], content: Node): Node =
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
}
