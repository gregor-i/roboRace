var _ = require('lodash')
var lobbyService = require('./lobby-service')

function actions(state, action) {
    if (action.createGame) {
        lobbyService.createGame()
    } else if (action.deleteGame) {
      lobbyService.deleteGame(action.deleteGame)
    }else if (action.redirectTo) {
        window.location.href = action.redirectTo
    } else if (action.definePlayerName) {
        localStorage.setItem('playerName', action.definePlayerName)
        return Promise.resolve(Object.assign({}, state, {player: action.definePlayerName}))
    }else if(action.reloadGameList) {
        return lobbyService.getAllGames().then(function (gameList) {
            return Object.assign({}, state, {games: gameList})
        })
    }else if(action.resetUserName){
        localStorage.removeItem('playerName')
        delete state.player
        return Promise.resolve(state)
    } else {
        console.error("unknown action", action)
    }
}

module.exports = actions