import {Editor} from './editor/editor'
import {Lobby} from './lobby/lobby'
import {Game} from "./game/game";

function container(){
  return document.getElementById('robo-race')
}

export function goToLobby(){
  const player = document.body.dataset.sessionId
  Lobby(container().firstElementChild, player)
}

export function goToGame(gameRow, scenarioRow){
  Game(container().firstElementChild, gameRow, scenarioRow)
}

export function goToEditor(scenarioId){
  Editor(container().firstElementChild, scenarioId)
}

document.addEventListener('DOMContentLoaded', goToLobby)
