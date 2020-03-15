//package roborace.frontend.gameBoard
//
//import com.raquo.snabbdom.Modifier
//import com.raquo.snabbdom.simple.{VNode, VNodeData}
//
//// note: this has to be the appended after the last child.
//class NameSpace(nameSpace: String) extends Modifier[VNode, VNodeData] {
//  import scala.language.dynamics
//
//  override def apply(v1: VNode): Unit = {
//    v1.data.asInstanceOf[scala.scalajs.js.Dynamic].ns = nameSpace
//    if(v1.maybeChildren.isDefined)
//      v1.maybeChildren.get.foreach(apply)
//  }
//}
//
//object NameSpace{
//  def apply(s: String): Modifier[VNode, VNodeData] =
//    new NameSpace(s)
//}
