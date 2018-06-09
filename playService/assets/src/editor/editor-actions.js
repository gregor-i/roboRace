const _ = require('lodash')
const editorService = require('./editor-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.backToLobby)
    window.location.href = "/"
  else if(action.setWidth){
    state.scenario.width = action.setWidth
    return Promise.resolve(state)
  }else if(action.setHeight){
    state.scenario.height = action.setHeight
    return Promise.resolve(state)
  }else{
    console.error("unknown action", action)
  }

}

module.exports = actions