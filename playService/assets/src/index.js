var Lobby = require('./lobby/lobby')
var Game = require('./game/game')

document.addEventListener('DOMContentLoaded', function () {
    var container = document.getElementById('robo-race')
    var player = localStorage.getItem('playerName')
    var gameId = document.body.dataset.gameId;
    if(gameId && player)
        Game(container, player, gameId)
    else
        Lobby(container, player)
})
