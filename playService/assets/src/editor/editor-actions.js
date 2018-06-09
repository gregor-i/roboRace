const _ = require('lodash')
const editorService = require('./editor-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.backToLobby) {
    window.location.href = "/"
  } else if (action.setModal) {
    state.modal = action.setModal
    return Promise.resolve(state)
  } else if (action.closeModal) {
    delete state.modal
    return Promise.resolve(state)
  } else if (action.editScenario) {
    window.location.href = "/editor/" + action.editScenario.id
  }else if (action.deleteScenario) {
    editorService.deleteScenario(action.deleteScenario)

  } else if (action === 'width++') {
    state.scenario.width++
    return Promise.resolve(state)
  } else if (action === 'width--') {
    state.scenario.width--
    return Promise.resolve(state)
  } else if (action === 'height++') {
    state.scenario.height++
    return Promise.resolve(state)
  } else if (action === 'height--') {
    state.scenario.height--
    return Promise.resolve(state)

  } else if (action === 'save') {
    editorService.postScenario(state.scenario)
        .then(row => window.location = "/editor/" + row.id)
  } else {
    console.error("unknown action", action)
  }

}

module.exports = actions
