package roborace.frontend

import api.GameResponse
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{EventSource, MessageEvent}
import roborace.frontend.pages.ErrorState
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyState
import roborace.frontend.service.Service
import snabbdom.{Snabbdom, SnabbdomFacade, VNode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.|

class RoboRaceApp(container: HTMLElement) {
  var node: HTMLElement | VNode = container

  val patch: SnabbdomFacade.PatchFunction = Snabbdom.init(
    classModule = true,
    propsModule = true,
    attributesModule = true,
    datasetModule = true,
    styleModule = true,
    eventlistenersModule = true
  )

  private def saveStateToHistory(state: PageState): Unit = {
    Router.stateToUrl(state) match {
      case Some((currentPath, currentSearch)) =>
        val stringSearch = Router.queryParamsToUrl(currentSearch)
        if (dom.window.location.pathname != currentPath) {
          dom.window.scroll(0, 0)
          dom.window.history.pushState(null, "", currentPath + stringSearch)
        } else {
          dom.window.history.replaceState(null, "", currentPath + stringSearch)
        }
      case None => ()
    }
  }

  var gameEventSource: Option[EventSource] = None
  def gameUpdates(globalState: GlobalState, state: PageState): Unit = {
    def eventListener(gameState: GameState): js.Function1[MessageEvent, Unit] = message => {
      decode[GameResponse](message.data.asInstanceOf[String]) match {
        case Right(newGame) => renderState(globalState, GameState.clearSlots(gameState, gameState.copy(game = newGame)))
        case Left(_)        => renderState(globalState, ErrorState("unexpected Message received on SSE"))
      }
    }

    (state, gameEventSource) match {
      case (state: GameState, Some(eventSource)) =>
        eventSource.onmessage = eventListener(state)

      case (state: GameState, None) =>
        val eventSource = Service.gameUpdates(state.game.id)
        eventSource.onmessage = eventListener(state)
        gameEventSource = Some(eventSource)

      case (_, Some(eventSource)) =>
        eventSource.close()
        gameEventSource = None

      case (_, None) => ()
    }
  }

  var lobbyEventSource: Option[EventSource] = None
  def lobbyUpdates(globalState: GlobalState, state: PageState): Unit = {
    def eventListener(lobbyState: LobbyState): js.Function1[MessageEvent, Unit] = message => {
      decode[Seq[GameResponse]](message.data.asInstanceOf[String]) match {
        case Right(newGames) => renderState(globalState, lobbyState.copy(games = newGames))
        case Left(_)         => renderState(globalState, ErrorState("unexpected Message received on SSE"))
      }
    }

    (state, lobbyEventSource) match {
      case (state: LobbyState, Some(eventSource)) =>
        eventSource.onmessage = eventListener(state)

      case (state: LobbyState, None) =>
        val eventSource = Service.lobbyUpdates()
        eventSource.onmessage = eventListener(state)
        lobbyEventSource = Some(eventSource)

      case (_, Some(eventSource)) =>
        eventSource.close()
        lobbyEventSource = None

      case (_, None) => ()
    }
  }

  def renderState(globalState: GlobalState, state: PageState): Unit = {
    saveStateToHistory(state)
    gameUpdates(globalState, state)
    lobbyUpdates(globalState, state)

    val context = Context(state, globalState, renderState)

    node = patch(node, Pages.ui(context).toVNode)
  }

  private def loadUserAndRenderFromLocation(): Unit =
    for (user <- Service.whoAmI()) yield renderState(GlobalState.initial, Router.stateFromUrl(GlobalState.initial, dom.window.location))

  dom.window.onpopstate = _ => loadUserAndRenderFromLocation()

  loadUserAndRenderFromLocation()
}
