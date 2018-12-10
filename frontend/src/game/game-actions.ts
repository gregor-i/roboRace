import * as _ from 'lodash'
import {joinGame, quitGame, resetInstruction, setInstruction} from "./game-service";
import {goToLobby} from "../index";
import {createGame} from "../lobby/lobby-service";
import {numberOfInstructionsPerCycle} from "../common/constants";

export function actions(state, action) {
  if (action.leaveGame) {
    if (state.game && state.game.you && !state.game.you.finished) {
      return quitGame(state.game.id)
          .then(newGameState => ({...state, game: newGameState}))
    } else {
      goToLobby()
    }
  } else if (action.createGame !== undefined){
    return createGame(state.scenario.scenario, action.createGame)
        .then(game => ({game, scenario: undefined}))
  } else if (action.joinGame)
    return joinGame(state.game.id, action.joinGame)
        .then(newGameState => ({...state, game: newGameState}))
  else if (action.focusSlot !== undefined) {
    return Promise.resolve({...state, focusedSlot: action.focusSlot})
  } else if (action.resetSlot) {
    return resetInstruction(state.game.id, state.game.cycle, action.slot)
        .then(newGameState => ({...state, game: newGameState}))
  } else if (action.setInstruction) {
    return setInstruction(state.game.id, state.game.cycle, action.slot, action.instruction)
        .then(newGameState => ({
          ...state,
          game: newGameState,
          focusedSlot: newGameState.cycle !== state.game.cycle ? 0 :
              _.range(numberOfInstructionsPerCycle)
                  .map(i => (i + (state.focusedSlot || 0)) % numberOfInstructionsPerCycle)
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
