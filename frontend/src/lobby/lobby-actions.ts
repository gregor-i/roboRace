import {goToEditor, goToGame, goToPreview} from "../index";
import {deleteGame, deleteScenario} from '../robo-race-service'
import {LobbyState} from "../state";

export function actions(state, action): Promise<LobbyState> {
  if(action.editScenario){
    goToEditor(action.editScenario)
  } else if (action.deleteGame) {
    deleteGame(action.deleteGame)
  }else if(action.deleteScenario){
    deleteScenario(action.id)
      .then(() => window.location.reload())
  }else if(action.openScenario){
    goToPreview(action.openScenario)
  }else if(action.openGame){
    goToGame(action.openGame)
  } else {
    console.error("unknown action", action)
    return Promise.resolve(state);
  }
}