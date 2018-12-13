package frontend.util

import com.raquo.snabbdom
import com.raquo.snabbdom.nodes.IterableNode
import com.raquo.snabbdom.simple.VNode

trait Ui {
  def seq(s: Seq[VNode]) = new IterableNode(s.map(snabbdom.nodeToChildNode))
}

object Ui extends Ui