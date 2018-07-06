const _ = require('lodash')
const gameService = require('./game-service')
const constants = require('../common/constants')

function actions(state, action) {
  if (action.leaveGame)
    window.location.href = "/"
  else if (action.joinGame)
    gameService.joinGame(action.joinGame)
  else if (action.focusSlot !== undefined) {
    state.focusedSlot = action.focusSlot
    return Promise.resolve(state)
  }else if(action.resetSlot){
    console.log("reset")
    return gameService.resetInstruction(state.gameId, state.game.cycle, action.slot)
  } else if (action.setInstruction) {
    // todo: update focus
    // state.focusedSlot = state.focusedSlot || 0
    // let o = state.focusedSlot
    // for(let i = 0; i< constants.numberOfInstructionsPerCycle; i++){
    //   if( state.slots[(i+o) % constants.numberOfInstructionsPerCycle] === null) {
    //     state.focusedSlot = (i+o) % constants.numberOfInstructionsPerCycle
    //     break;
    //   }
    // }
    return gameService.setInstruction(state.gameId, state.game.cycle, action.slot, action.instruction)
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
