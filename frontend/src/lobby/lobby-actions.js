const _ = require('lodash')
const lobbyService = require('./lobby-service')
const editorService = require('../editor/editor-service')
const Cookie = require('js-cookie')

function actions(state, action) {
  if (action.createGame) {
    lobbyService.createGame(action.createGame)
      .then(resp => window.location.href = "/game/" + (resp.id))
  } else if (action.previewScenario) {
    state.previewScenario = action.previewScenario
    return Promise.resolve(state)
  }else if(action.closeModal) {
    state.previewScenario = null
    return Promise.resolve(state)
  }else if(action.editScenario){
    window.location.href = "/editor/" + action.editScenario
  } else if (action.deleteGame) {
    lobbyService.deleteGame(action.deleteGame)
  }else if(action.deleteScenario){
    editorService.deleteScenario(action.id)
      .then(() => window.location.reload())
  } else if (action.redirectTo) {
    window.location.href = action.redirectTo
  } else if (action.definePlayerName) {
    const name = action.definePlayerName
    Cookie.set('playerName', name)
    return Promise.resolve(Object.assign({}, state, {player: name}))
  } else if (action.resetUserName) {
    Cookie.remove('playerName')
    delete state.player
    return Promise.resolve(state)
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
