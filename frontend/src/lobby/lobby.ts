import {init} from 'snabbdom'
import {classModule} from 'snabbdom/modules/class'
import {propsModule} from 'snabbdom/modules/props'
import {styleModule} from 'snabbdom/modules/style'
import {attributesModule} from 'snabbdom/modules/attributes'
import {eventListenersModule} from 'snabbdom/modules/eventlisteners'

import {render} from './lobby-ui'
import {actions} from './lobby-actions'
import {getAllGames, loadAllScenarios, lobbyUpdates} from '../robo-race-service'
import {LobbyState} from "../state";

const patch = init([
    classModule,
    propsModule,
    styleModule,
    eventListenersModule,
    attributesModule
])

export function Lobby(element, player) {
  let node = element
  const updates = lobbyUpdates()

  function renderState(state: LobbyState) {
    updates.onmessage = lobbyEventHandler(state)
    node = patch(node, render(state, actionHandler(state)))
  }

  function actionHandler(state: LobbyState) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  function lobbyEventHandler(state: LobbyState) {
    return function (event) {
      state.games = JSON.parse(event.data)
      renderState(state)
    }
  }

  getAllGames().then((games) =>
    loadAllScenarios().then((scenarios) =>
      renderState({games, scenarios})
    )
  )

  return this
}