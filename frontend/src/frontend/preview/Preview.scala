package frontend.preview

import frontend.PreviewState
import frontend.util.SnabbdomApp
import gameEntities.ScenarioResponse
import org.scalajs.dom.raw.Element

class Preview(container: Element, scenarioResponse: ScenarioResponse) extends SnabbdomApp{
  def renderState(state: PreviewState): Unit = {
    patch(container, PreviewUi.render(state))
  }

  renderState(PreviewState(scenarioResponse))
}
