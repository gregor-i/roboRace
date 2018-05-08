var _ = require('lodash')
var lobbyService = require('./lobby-service')

function actions(state, action) {
    if (action.createGame) {
        lobbyService.createGame()
    } else if (action.deleteGame) {
        lobbyService.deleteGame(action.deleteGame)
    } else if (action.enterGame) {
        window.location.href = "/"+action.enterGame
    } else if (action.definePlayerName) {
        localStorage.setItem('playerName', action.definePlayerName)
        return Promise.resolve(Object.assign({}, state, {player: action.definePlayerName}))
    }else if(action.reloadGameList){
        return lobbyService.getAllGames().then(function(gameList){
            return Object.assign({}, state, {games:gameList})
        })
    } else {
        console.error("unknown action", action)
    }
}

module.exports = actions