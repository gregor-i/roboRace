package roborace.frontend.components

import org.scalajs.dom.MouseEvent
import snabbdom.Node

object BulmaComponents {
  def singleColumn(mods: Node): Node =
    Node("div.columns.is-centered.is-mobile")
      .child(
        Node("div.column.single-column").child(mods)
      )

//  def card(content: VNode, actions: (String, Option[MouseEvent => Unit])*): VNode =
//    div(className := "card",
//      margin := "8px",
//      div(className := "card-content", content),
//      cond(actions.nonEmpty, footer(className := "card-footer",
//        seq(actions.map {
//          case (node, Some(click)) => a(className := "card-footer-item", onClick := click, node)
//          case (node, None)        => span(className := "card-footer-item", node)
//        })
//      ))
//    )
//
//  def mediaObject(left: Option[VNode], content: VNode, mods: Modifier[VNode, VNodeData]*): VNode =
//    div(className := "media",
//      left.map(l =>
//        figure(className := "media-left",
//          p(className := "image",
//            width := "64px",
//            height := "64px",
//            l
//          ))
//      ),
//      div(className := "media-content", content),
//    ).apply(mods)
//
//  def tag(text: String, classes : String = ""): VNode =
//    span(className := s"tag $classes", text)

}
