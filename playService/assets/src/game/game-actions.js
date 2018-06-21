const _ = require('lodash')
const gameService = require('./game-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.leaveGame)
    window.location.href = "/"
  else if (action.joinGame)
    gameService.joinGame(action.joinGame)
  else if (action.readyForGame)
    gameService.readyForGame(action.readyForGame)
  else if (action.selectScenario)
    gameService.defineScenario(state.gameId, action.selectScenario)
  else if (action.focusAction !== undefined) {
    state.focusAction = action.focusAction
    return Promise.resolve(state)
  } else if (action.defineInstruction) {
    if (!state.slots)
      state.slots = []
    let slot = action.defineInstruction.slot
    state.slots[slot] = action.defineInstruction.value
    if (_.range(constants.numberOfInstructionsPerCycle).every(i => state.slots[i] >= 0))
      gameService.defineInstruction(state.gameId, action.defineInstruction.cycle, state.slots)
    delete state.focusAction
    return Promise.resolve(state)
  } else if (action.replayAnimations) {
    state.animationStart = new Date()
    return Promise.resolve(state)
  } else if (action.setModal) {
    state.modal = action.setModal
    return Promise.resolve(state)
  }else if (action.closeModal){
    delete state.modal
    return Promise.resolve(state)
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
