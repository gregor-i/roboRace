const Lobby = require('./lobby/lobby')
const Game = require('./game/game')
const Editor = require('./editor/editor')

document.addEventListener('DOMContentLoaded', function () {
  const container = document.getElementById('robo-race')
  const player = document.body.dataset.sessionId
  const mode = document.body.dataset.mode
  const gameId = document.body.dataset.gameId
  const scenarioId = document.body.dataset.scenarioId
  if (mode === "lobby")
    Lobby(container, player)
  else if (mode === "game")
    Game(container, player, gameId)
  else if (mode === "editor")
    Editor(container, player, scenarioId)
  else
    document.write('unknown mode')
})
