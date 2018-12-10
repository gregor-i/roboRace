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

import {updates} from "./game-service";
import {actions} from "./game-actions";
import {render} from "./game-ui";

export function Game(element, gameRow, scenarioRow) {
  let node = element
  let eventSource = gameRow ? updates(gameRow.id) : null

  function renderState(state) {
    if(!eventSource && state.game){
      eventSource = updates(state.game.id)
    }

    if (eventSource){
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

  function actionHandler(state) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  renderState({
    game:gameRow,
    scenario: scenarioRow,
    modal: 'none'
  })

  return this
}