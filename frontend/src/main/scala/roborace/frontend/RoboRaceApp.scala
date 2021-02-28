package roborace.frontend

import api.WithId
import entities.Game
import io.circe.generic.auto._
import io.circe.parser
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{EventSource, MessageEvent}
import roborace.frontend.pages.ErrorState
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyState
import roborace.frontend.service.Service
import snabbdom.{PatchFunction, Snabbdom, VNode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

class RoboRaceApp(container: HTMLElement) {
  var node: HTMLElement | VNode = container

  val patch: PatchFunction = Snabbdom.init(
    classModule = true,
    propsModule = true,
    attributesModule = true,
    datasetModule = true,
    styleModule = true,
    eventlistenersModule = true
  )

  private def saveLocalStateToHistory(state: PageState): Unit = {
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

  private def saveFinishedLevels(globalState: GlobalState): Unit =
    dom.window.localStorage.setItem("finishedSinglePlayerLevels", globalState.finishedSinglePlayerLevels.asJson.noSpaces)

  private def loadFinishedLevels(): Option[Set[String]] =
    dom.window.localStorage
      .getItem("finishedSinglePlayerLevels")
      .asInstanceOf[UndefOr[String]]
      .toOption
      .flatMap(parser.decode[Set[String]](_).toOption)

  var gameEventSource: Option[EventSource] = None
  def gameUpdates(globalState: GlobalState, state: PageState): Unit = {
    def eventListener(gameState: GameState): js.Function1[MessageEvent, Unit] = message => {
      decode[WithId[Game]](message.data.asInstanceOf[String]) match {
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
      decode[Seq[WithId[Game]]](message.data.asInstanceOf[String]) match {
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
    saveLocalStateToHistory(state)
    saveFinishedLevels(globalState)
    gameUpdates(globalState, state)
    lobbyUpdates(globalState, state)

    val context = Context(state, globalState, renderState)

    node = patch(node, Pages.ui(context).toVNode)
  }

  private def loadUserAndRenderFromLocation(): Unit =
    for (sessionId <- Service.getSessionId()) {
      val finishedlevels = loadFinishedLevels()
      val globalState = GlobalState(
        sessionId = sessionId,
        finishedSinglePlayerLevels = finishedlevels.getOrElse(Set.empty)
      )
      val localState = Router.stateFromUrl(globalState, dom.window.location)
      renderState(globalState, localState)
    }

  dom.window.onpopstate = _ => loadUserAndRenderFromLocation()

  loadUserAndRenderFromLocation()
}
