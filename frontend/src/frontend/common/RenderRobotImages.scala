package frontend.common

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, src}
import com.raquo.snabbdom.simple.tags.img
import com.raquo.snabbdom.simple.events.onClick
import org.scalajs.dom.Event

object RenderRobotImages {
  def apply(index: Int, filled: Boolean, you: Boolean, click: Option[Event => Unit]): VNode =
    img(
      className := "robot-tile",
      if (you) Some(className := "robot-tile-you") else None,
      src := (if (filled) Images.player(index) else Images.playerStart(index)),
      click.map(onClick := _)
    )
}
