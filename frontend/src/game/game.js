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

function Game(element, player, gameId) {
  var node = element

  function renderState(state) {
    state.eventSource.onmessage = gameEventHandler(state)

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

  function gameEventHandler(state) {
    return function (event) {
      const serverState = JSON.parse(event.data)
      const newCycle = state.game.cycle !== serverState.cycle

      state.game = serverState
      if (newCycle) {
        state.focusedSlot = 0
      }
      renderState(state)
    }
  }

  return gameService.getState(gameId)
    .then(game => {
      renderState({
        player, gameId, game,
        eventSource: gameService.updates(gameId),
        modal: 'none'
      }, element)
    }).catch(function (ex) {
      console.error(ex)
    })

  return this
}

module.exports = Game
