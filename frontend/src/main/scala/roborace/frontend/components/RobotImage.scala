//package roborace.frontend.components
//
//import com.raquo.snabbdom.Modifier
//import com.raquo.snabbdom.simple.implicits._
//import com.raquo.snabbdom.simple.props.{className, src}
//import com.raquo.snabbdom.simple.tags.img
//import com.raquo.snabbdom.simple.{VNode, VNodeData}
//
//object RobotImage {
//  def apply(index: Int,
//            filled: Boolean,
//            mods: Modifier[VNode, VNodeData]*): VNode =
//    img(
//      className := "robot-tile",
//      src := (if (filled) Images.player(index) else Images.playerStart(index))
//    ).apply(mods)
//}
