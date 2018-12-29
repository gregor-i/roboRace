package frontend.editor


import com.raquo.snabbdom.simple.VNode
import frontend.Service
import frontend.util.SnabbdomApp
import gameEntities._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom.raw.Element

import scala.scalajs.js.|

class Editor(container: Element, resp: ScenarioResponse) extends SnabbdomApp {

  var node: Element | VNode = container

  def renderState(state: EditorState): Unit = {
    node = patch(node, EditorUi(state, renderState))
  }

  renderState(EditorState(
    scenario = resp.scenario,
    clickAction = None,
    description = resp.description
  ))
}
