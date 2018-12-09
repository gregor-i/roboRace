const _ = require('lodash')
const gameService = require('./game-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.leaveGame) {
    if (state.game.you && !state.game.you.finished) {
      return gameService.quitGame(state.gameId)
          .then(newGameState => ({...state, game: newGameState}))
    } else {
      require('../index').goToLobby()
    }
  } else if (action.joinGame)
    return gameService.joinGame(action.joinGame)
        .then(newGameState => ({...state, game: newGameState}))
  else if (action.focusSlot !== undefined) {
    return Promise.resolve({...state, focusedSlot: action.focusSlot})
  } else if (action.resetSlot) {
    return gameService.resetInstruction(state.gameId, state.game.cycle, action.slot)
        .then(newGameState => ({...state, game: newGameState}))
  } else if (action.setInstruction) {
    return gameService.setInstruction(state.gameId, state.game.cycle, action.slot, action.instruction)
        .then(newGameState => ({
          ...state,
          game: newGameState,
          focusedSlot: newGameState.cycle !== state.game.cycle ? 0 :
              _.range(constants.numberOfInstructionsPerCycle)
                  .map(i => (i + (state.focusedSlot || 0)) % constants.numberOfInstructionsPerCycle)
                  .find(i => newGameState.you.instructionSlots[i] === null)
        }))
  } else if (action.replayAnimations) {
    return Promise.resolve({...state, animationStart: new Date()})
  } else if (action.setModal) {
    return Promise.resolve({...state, modal: action.setModal})
  } else if (action.closeModal) {
    return Promise.resolve({...state, modal: undefined})
  } else {
    console.error("unknown action", action)
  }
}

module.exports = actions
