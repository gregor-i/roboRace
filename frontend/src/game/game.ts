import {init} from 'snabbdom'

import {classModule} from 'snabbdom/modules/class'
import {propsModule} from 'snabbdom/modules/props'
import {styleModule} from 'snabbdom/modules/style'
import {attributesModule} from 'snabbdom/modules/attributes'
import {eventListenersModule} from 'snabbdom/modules/eventlisteners'

const patch = init([
  classModule,
  propsModule,
  styleModule,
  eventListenersModule,
  attributesModule
])

import {gameUpdates} from '../robo-race-service'
import {actions} from './game-actions'
import {render} from './game-ui'
import {GameState} from "../state";
import {Game, ScenarioRow} from "../models";

export function Game(element, gameRow?: Game, scenarioRow?: ScenarioRow) {
  let node = element
  let eventSource = gameRow ? gameUpdates(gameRow.id) : null

  function renderState(state: GameState) {
    if (!eventSource && state.game) {
      eventSource = gameUpdates(state.game.id)
    }

    if (eventSource) {
      eventSource.onmessage = function (event) {
        const serverState = JSON.parse(event.data)
        const newCycle = state.game.cycle !== serverState.cycle

        state.game = serverState
        if (newCycle) {
          state.focusedSlot = 0
        }
        renderState(state)
      }
    }

    node = patch(node, render(state, actionHandler(state)))
  }

  function actionHandler(state: GameState) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  renderState({
    game: gameRow,
    scenario: scenarioRow
  })

  return this
}