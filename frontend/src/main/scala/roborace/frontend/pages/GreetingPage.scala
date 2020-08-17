package roborace.frontend.pages

import api.User
import logic.DefaultScenario
import org.scalajs.dom
import roborace.frontend.FrontendState
import roborace.frontend.pages.components.gameBoard.RenderScenario
import roborace.frontend.pages.components.{Body, Button, ButtonList, Images, Modal}
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.pages.singleplayer.SelectLevelState
import roborace.macros.StaticContent
import snabbdom.{Node, Snabbdom}

case class GreetingState(user: Option[User]) extends FrontendState

object GreetingPage extends Page[GreetingState] {
  override def stateFromUrl = {
    case (user, "/", _) => GreetingState(user)
  }

  override def stateToUrl(state: GreetingState): Option[(Path, QueryParameter)] =
    Some("/" -> Map.empty)

  def render(implicit state: GreetingState, update: Update) =
    Body()
      .child(content)

  private def content(implicit state: GreetingState, update: Update) = {
    Modal(closeAction = Snabbdom.event(_ => ()), background = background)(
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

  private def buttons(implicit update: Update) =
    ButtonList.fullWidth(
      Button("Singleplayer", Snabbdom.event(_ => update(SelectLevelState)))
        .classes("button", "is-link", "is-outlined"),
      Button("Multiplayer", Snabbdom.event(_ => update(LobbyPage.load())))
        .classes("button", "is-link", "is-outlined")
    )
}
