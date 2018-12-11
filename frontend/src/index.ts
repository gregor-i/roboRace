import {Editor} from './editor/editor'
import {Lobby} from './lobby/lobby'
import {Game} from "./game/game";
import {Preview} from "./preview/preview";
import {ScenarioRow} from "./models";

function container(){
  return document.getElementById('robo-race').firstElementChild
}

export function goToLobby(){
  const player = document.body.dataset.sessionId
  Lobby(container(), player)
}

export function goToGame(gameRow){
  Game(container(), gameRow)
}

export function goToPreview(scenarioRow: ScenarioRow){
  Preview(container(), scenarioRow)
}

export function goToEditor(scenarioId){
  Editor(container(), scenarioId)
}

document.addEventListener('DOMContentLoaded', goToLobby)
