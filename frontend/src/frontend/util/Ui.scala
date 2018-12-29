package frontend.util

import com.raquo.snabbdom
import com.raquo.snabbdom.nodes.IterableNode
import com.raquo.snabbdom.simple.VNode

trait Ui {
  def seq(s: Seq[VNode]) = new IterableNode(s.map(snabbdom.nodeToChildNode))

  def cond(b: Boolean, node: => VNode): Option[VNode] = if(b) Some(node) else None
}

object Ui extends Ui
