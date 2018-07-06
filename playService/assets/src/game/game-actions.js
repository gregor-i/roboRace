const _ = require('lodash')
const gameService = require('./game-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.leaveGame)
    window.location.href = "/"
  else if (action.joinGame)
    return gameService.joinGame(action.joinGame)
        .then(newGameState => {
          state.game = newGameState
          return state
        })
  else if (action.focusSlot !== undefined) {
    state.focusedSlot = action.focusSlot
    return Promise.resolve(state)
  } else if (action.resetSlot) {
    return gameService.resetInstruction(state.gameId, state.game.cycle, action.slot)
        .then(newGameState => {
          state.game = newGameState
          return state
        })
  } else if (action.setInstruction) {
    return gameService.setInstruction(state.gameId, state.game.cycle, action.slot, action.instruction)
        .then(newGameState => {
          state.game = newGameState
          const slots = state.game.players.find(p => p.name === state.player).instructionSlots
          state.focusedSlot = _.range(constants.numberOfInstructionsPerCycle)
              .map(i => (i + (state.focusedSlot || 0)) % constants.numberOfInstructionsPerCycle)
              .find(i => slots[i] === null)
          return state
        })
  } else if (action.replayAnimations) {
    state.animationStart = new Date()
    return Promise.resolve(state)
  } else if (action.setModal) {
    state.modal = action.setModal
    return Promise.resolve(state)
  } else if (action.closeModal) {
    delete state.modal
    return Promise.resolve(state)
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
