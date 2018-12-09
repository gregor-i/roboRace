const _ = require('lodash')
const lobbyService = require('./lobby-service')
const editorService = require('../editor/editor-service')

function actions(state, action) {
  if (action.createGame) {
    lobbyService.createGame(action.createGame)
  } else if (action.previewScenario) {
    state.previewScenario = action.previewScenario
    return Promise.resolve(state)
  }else if(action.closeModal) {
    state.previewScenario = null
    return Promise.resolve(state)
  }else if(action.editScenario){
    require('../index').goToEditor(action.editScenario)
  } else if (action.deleteGame) {
    lobbyService.deleteGame(action.deleteGame)
  }else if(action.deleteScenario){
    editorService.deleteScenario(action.id)
      .then(() => window.location.reload())
  }else if(action.openGame){
    require('../index').goToGame(action.openGame)
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
