const Lobby = require('./lobby/lobby')
const Game = require('./game/game')
const Editor = require('./editor/editor')

document.addEventListener('DOMContentLoaded', function () {
  const container = document.getElementById('robo-race')
  const player = localStorage.getItem('playerName')
  const mode = document.body.dataset.mode
  const gameId = document.body.dataset.gameId
  if (mode === "lobby")
    Lobby(container, player)
  else if (mode === "game")
    Game(container, player, gameId)
  else if (mode === "editor")
    Editor(container)
  else
    document.write('unknown mode')
})
