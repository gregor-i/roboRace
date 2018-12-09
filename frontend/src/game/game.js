const _ = require('lodash')
const snabbdom = require('snabbdom')
const patch = snabbdom.init([
  require('snabbdom/modules/eventlisteners').default,
  require('snabbdom/modules/props').default,
  require('snabbdom/modules/class').default,
  require('snabbdom/modules/style').default,
  require('snabbdom/modules/attributes').default,
])

const gameUi = require('./game-ui')
const gameService = require('./game-service')
const actions = require('./game-actions')

function Game(element, gameRow, scenarioRow) {
  var node = element
  var eventSource = gameRow ? gameService.updates(gameRow.id) : null

  function renderState(state) {
    if(!eventSource && state.game){
      eventSource = gameService.updates(state.game.id)
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

    window.currentState = state
    node = patch(node, gameUi(state, actionHandler(state)))
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
  }, element)

  return this
}

module.exports = Game
