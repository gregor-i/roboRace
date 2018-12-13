package frontend.common

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.events.onClick
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, src}
import com.raquo.snabbdom.simple.tags.{div, img}

object Fab {
  def apply(classes: String, image: String, click: () => Unit): VNode = {
    div(className := "fab " + classes,
      onClick := click,
      img(
        src := image
      )
    )
  }
}
