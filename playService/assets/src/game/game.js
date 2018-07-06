const _ = require('lodash')
const snabbdom = require('snabbdom')
const patch = snabbdom.init([
  require('snabbdom/modules/eventlisteners').default,
  require('snabbdom/modules/props').default,
  require('snabbdom/modules/class').default,
  require('snabbdom/modules/style').default
])

const gameUi = require('./game-ui')
const gameService = require('./game-service')
const actions = require('./game-actions')
const gameBoard = require('./game-board')

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
      const oldState = state.game
      const newEvents = _.drop(serverState.events, oldState.events.length)

      state.animationStart = new Date()
      state.animations = gameBoard.framesFromEvents(oldState, newEvents)
      state.game = serverState
      if (newEvents.find((event) => !!event.StartNextCycle || !!event.AllPlayersFinished)) {
        state.focusedSlot = undefined
      }
      renderState(state)
    }
  }

  return gameService.getState(gameId)
    .then(gameRow => {
      renderState({
        player, gameId,
        game: gameRow.game,
        eventSource: gameService.updates(gameId),
        modal: 'none',
        animations: [],
        animationStart: undefined
      }, element)
    }).catch(function (ex) {
      console.error(ex)
    })

  return this
}

module.exports = Game
