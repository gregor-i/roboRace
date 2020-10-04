package roborace.frontend.pages

import logic.DefaultScenario
import roborace.frontend.PageState
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Button, ButtonList, Modal}
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.pages.singleplayer.SelectLevelState
import roborace.frontend.util.SnabbdomEventListener
import roborace.macros.StaticContent
import snabbdom.Node

case class GreetingState() extends PageState

object GreetingPage extends Page[GreetingState] {
  override def stateFromUrl = {
    case (_, "/", _) => GreetingState()
  }

  override def stateToUrl(state: GreetingState): Option[(Path, QueryParameter)] =
    Some("/" -> Map.empty)

  override def render(implicit context: Context) =
    Body()
      .child(content)

  private def content(implicit context: Context) = {
    Modal(closeAction = SnabbdomEventListener.noop, background = background)(
      text,
      buttons
    )
  }

  private def background =
    Some {
      RenderScenario
        .svg(DefaultScenario.default, None)
        .attr("width", "100%")
        .attr("heigt", "100%")
        .style("opacity", "0.5")
    }

  private def text =
    Node("div.content").prop("innerHTML", StaticContent("frontend/src/main/html/greeting.html"))

  private def buttons(implicit context: Context) =
    ButtonList.fullWidth(
      Button("Singleplayer", SnabbdomEventListener.set(SelectLevelState()))
        .classes("button", "is-link", "is-outlined"),
      Button("Multiplayer", SnabbdomEventListener.set(LobbyPage.load()))
        .classes("button", "is-link", "is-outlined")
    )
}
