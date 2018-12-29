package frontend.common

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.className
import com.raquo.snabbdom.simple.styles.{height, marginBottom, width}
import com.raquo.snabbdom.simple.tags._
import frontend.util.Ui


object BulmaComponents extends Ui{
  def card(content: VNode, actions: Seq[VNode]): VNode =
    div(className := "card",
      marginBottom := "16px",
      div(className := "card-content", content),
      cond(actions.nonEmpty, footer(className := "card-footer",
        seq(actions.map(p(className := "card-footer-item", _)))
      ))
    )

  def mediaObject(left: Option[VNode], content: VNode): VNode =
    div(className := "media",
      left.map(l =>
        figure(className := "media-left",
          p(className := "image",
            width := "64px",
            height := "64px",
            l
          ))
      ),
      div(className := "media-content", content),
    )

  def tag(text: String, classes : String = ""): VNode =
    span(className := s"tag $classes", text)

}
