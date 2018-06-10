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
  }else if(action.setClickAction) {
    state.clickAction = action.setClickAction
    return Promise.resolve(state)
  }else if(action.togglePit) {
    const {x, y} = action.togglePit
    if (state.scenario.pits.find(p => p.x === x && p.y === y))
      state.scenario.pits = state.scenario.pits.filter(p => p.x !== x || p.y !== y)
    else
      state.scenario.pits = [...state.scenario.pits, {x, y}]
    return Promise.resolve(state)
  }else if(action.setTarget) {
    state.scenario.targetPosition = action.setTarget
    return Promise.resolve(state)
  }else if(action.toggleInitialRobot) {
    const {x, y} = action.toggleInitialRobot
    if (state.scenario.initialRobots.find(r => r.position.x === x && r.position.y === y))
      state.scenario.initialRobots = state.scenario.initialRobots.filter(r => r.position.x !== x || r.position.y !== y)
    else
      state.scenario.initialRobots = [...state.scenario.initialRobots, {position: {x, y}, direction: {Up: {}}}]
    return Promise.resolve(state)
  }else if(action.rotateRobot){
    const {x, y} = action.rotateRobot
    function rot(dir){
      if(dir.Up)
        return {UpRight: {}}
      else if(dir.UpRight)
        return {DownRight: {}}
      else if(dir.DownRight)
        return {Down: {}}
      else if(dir.Down)
        return {DownLeft: {}}
      else if(dir.DownLeft)
        return {UpLeft: {}}
      else if(dir.UpLeft)
        return {Up: {}}
    }
    if (state.scenario.initialRobots.find(r => r.position.x === x && r.position.y === y))
      state.scenario.initialRobots = state.scenario.initialRobots.map(r => r.position.x === x && r.position.y === y ? {position: r.position, direction: rot(r.direction)} : r)
    return Promise.resolve(state)

  } else if (action === 'save') {
    editorService.postScenario(state.scenario)
        .then(row => window.location = "/editor/" + row.id)
  } else {
    console.error("unknown action", action)
  }

}

module.exports = actions
