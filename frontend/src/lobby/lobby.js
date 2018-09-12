const snabbdom = require('snabbdom')
const patch = snabbdom.init([
  require('snabbdom/modules/eventlisteners').default,
  require('snabbdom/modules/props').default,
  require('snabbdom/modules/class').default,
  require('snabbdom/modules/style').default,
  require('snabbdom/modules/attributes').default,
])

const lobbyUi = require('./lobby-ui')
const lobbyService = require('./lobby-service')
const editorService = require('../editor/editor-service')
const actions = require('./lobby-actions')

function Lobby(element, player) {
  let node = element
  const lobbyUpdates = lobbyService.updates()

  function renderState(state) {
    lobbyUpdates.onmessage = lobbyEventHandler(state)
    window.currentState = state
    node = patch(node, lobbyUi(state, actionHandler(state)))
  }

  function actionHandler(state) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  function lobbyEventHandler(state) {
    return function (event) {
      state.games = JSON.parse(event.data)
      renderState(state)
    }
  }

  lobbyService.getAllGames().then((games) =>
    editorService.loadAllScenarios().then((scenarios) =>
      renderState({player, games, scenarios}, element)
    )
  )

  return this
}

module.exports = Lobby
