const _ = require('lodash')
const lobbyService = require('./lobby-service')
const Cookie = require('js-cookie')

function actions(state, action) {
    if (action.createGame) {
        lobbyService.createGame()
    } else if (action.deleteGame) {
      lobbyService.deleteGame(action.deleteGame)
    }else if (action.redirectTo) {
        window.location.href = action.redirectTo
    } else if (action.definePlayerName) {
        const name = action.definePlayerName
        Cookie.set('playerName', name)
        return Promise.resolve(Object.assign({}, state, {player: name}))
    }else if(action.resetUserName){
        Cookie.remove('playerName')
        delete state.player
        return Promise.resolve(state)
    } else {
        console.error("unknown action", action)
    }
}

module.exports = actions
