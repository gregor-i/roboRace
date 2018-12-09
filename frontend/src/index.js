const Lobby = require('./lobby/lobby')
const Game = require('./game/game')
const Editor = require('./editor/editor')

function container(){
  return document.getElementById('robo-race')
}

function goToLobby(){
  const player = document.body.dataset.sessionId
  Lobby(container().firstElementChild, player)
}

function goToGame(gameRow, scenarioRow){
  Game(container().firstElementChild, gameRow, scenarioRow)
}

function goToEditor(scenarioId){
  Editor(container().firstElementChild, player, scenarioId)
}

document.addEventListener('DOMContentLoaded', goToLobby)

module.exports = {
  goToLobby,
  goToGame,
  goToEditor
}
