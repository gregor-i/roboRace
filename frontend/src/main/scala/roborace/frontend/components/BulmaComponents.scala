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
//        cond(
//          actions.nonEmpty,
//          footer(
//            className := "card-footer",
//            seq(actions.map {
//              case (node, Some(click)) => a(className := "card-footer-item", onClick := click, node)
//              case (node, None)        => span(className := "card-footer-item", node)
//            })
//          )
//        )
      )

  def mediaObject(left: Option[Node], content: Node, mods: Node*): Node =
    Node("div.media")
      .childOptional(
        left.map(
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
//        .child(
//      div(className := "media-content", content),
//    )
//  .apply(mods)

//  def tag(text: String, classes : String = ""): VNode =
//    span(className := s"tag $classes", text)

}
