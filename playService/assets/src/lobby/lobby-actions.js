const _ = require('lodash')
const lobbyService = require('./lobby-service')
const Cookie = require('js-cookie')

function actions(state, action) {
    if (action.createGame) {
        lobbyService.createGame()
    } else if (action.deleteGame) {
        lobbyService.deleteGame(action.deleteGame)
    } else if (action.enterGame) {
        window.location.href = "/"+action.enterGame
    } else if (action.definePlayerName) {
        const name = action.definePlayerName
        Cookie.set('playerName', name)
        return Promise.resolve(Object.assign({}, state, {player: name}))
    }else if(action.reloadGameList) {
        return lobbyService.getAllGames().then(function (gameList) {
            return Object.assign({}, state, {games: gameList})
        })
    }else if(action.resetUserName){
        Cookie.remove('playerName')
        delete state.player
        return Promise.resolve(state)
    } else {
        console.error("unknown action", action)
    }
}

module.exports = actions