import {goToEditor, goToGame} from "../index";
import {deleteGame, deleteScenario} from '../robo-race-service'

export function actions(state, action) {
  if (action.previewScenario) {
    state.previewScenario = action.previewScenario
    return Promise.resolve(state)
  }else if(action.closeModal) {
    state.previewScenario = null
    return Promise.resolve(state)
  }else if(action.editScenario){
    goToEditor(action.editScenario)
  } else if (action.deleteGame) {
    deleteGame(action.deleteGame)
  }else if(action.deleteScenario){
    deleteScenario(action.id)
      .then(() => window.location.reload())
  }else if(action.openScenario){
    goToGame(undefined, action.openScenario)
  }else if(action.openGame){
    goToGame(action.openGame, undefined)
  } else {
    console.error("unknown action", action)
  }
}