package frontend.components

import com.raquo.snabbdom.Modifier
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, src}
import com.raquo.snabbdom.simple.tags.{div, img}
import com.raquo.snabbdom.simple.{VNode, VNodeData}

object Fab {
  def apply(classes: String, image: String, mods: Modifier[VNode, VNodeData]*): VNode = {
    div(className := "fab " + classes,
      img(src := image)
    ).apply(mods)
  }
}
