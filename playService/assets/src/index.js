const Lobby = require('./lobby/lobby')
const Game = require('./game/game')
const Cookie = require('js-cookie')

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('robo-race')
    const player = Cookie.get('playerName')
    const gameId = document.body.dataset.gameId
    if(gameId && player)
        Game(container, player, gameId)
    else
        Lobby(container, player)
})
