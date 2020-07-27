package roborace.frontend.lobby

import gameEntities.GameResponse
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom.raw.Element
import roborace.frontend.util.SnabbdomApp
import roborace.frontend.{LobbyFrontendState, Service}
import snabbdom.VNode

import scala.concurrent.ExecutionContext.Implicits.global

import scala.scalajs.js.|

class Lobby(container: Element) extends SnabbdomApp {

  var node: Element | VNode = container

  val eventSource = Service.lobbyUpdates()

  def renderState(state: LobbyFrontendState): Unit = {
    eventSource.onmessage = message => {
      val newGames = decode[Seq[GameResponse]](message.data.asInstanceOf[String]).right.get
      renderState(state.copy(games = newGames))
    }
    node = patch(node, LobbyUi.render(state).toVNode)

  }

  for {
    games     <- roborace.frontend.Service.getAllGames()
    scenarios <- roborace.frontend.Service.getAllScenarios()
  } renderState(LobbyFrontendState(games, scenarios))
}
