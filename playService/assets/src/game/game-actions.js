const _ = require('lodash')
const gameService = require('./game-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.leaveGame)
    window.location.href = "/"
  else if (action.joinGame)
    gameService.joinGame(action.joinGame, state.player)
  else if (action.readyForGame)
    gameService.readyForGame(action.readyForGame, state.player)
  else if (action.selectScenario)
    gameService.defineScenario(state.gameId, action.selectScenario)
  else if (action.focusAction !== undefined) {
    state.focusAction = action.focusAction
    return Promise.resolve(state)
  } else if (action.defineAction) {
    if (!state.slots)
      state.slots = []
    let slot = action.defineAction.slot
    state.slots[slot] = action.defineAction.value
    if (_.range(constants.numberOfActionsPerCycle).every(i => state.slots[i] >= 0))
      gameService.defineAction(state.gameId, state.player, action.defineAction.cycle, state.slots)
    delete state.focusAction
    return Promise.resolve(state)
  } else if (action.replayAnimations) {
    state.animationStart = new Date()
    return Promise.resolve(state)
  } else if (action.setModal) {
    state.modal = action.setModal
    return Promise.resolve(state)
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
