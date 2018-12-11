import {createGame} from '../robo-race-service'
import {goToGame, goToLobby} from "../index";
import {PreviewState} from "../state";

export function actions(state: PreviewState, action): Promise<PreviewState> {
  if (action.leaveGame) {
    goToLobby()
  } else if (action.createGame !== undefined) {
    createGame(state.scenarioRow.scenario, action.createGame)
      .then(game => goToGame(game))
  } else {
    console.error("unknown action", action)
    return Promise.resolve(state)
  }
}
