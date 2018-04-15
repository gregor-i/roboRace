var lobbyService = require('./lobby-service')
var gameService = require('./game-service')
var _ = require('lodash')
var constants = require('./constants')

function actions(state, action) {
    // lobby actions
    if (action.createGame) {
        lobbyService.createGame()
    } else if (action.deleteGame) {
        lobbyService.deleteGame(action.deleteGame)
    } else if (action.enterGame) {
        const gameId = action.enterGame
        const oldEvents = state.eventSource
        if (oldEvents)
            oldEvents.close()
        const newEvents = gameService.updates(gameId)
        const newState = Object.assign({}, state, {selectedGame: gameId, eventSource: newEvents, slots: []})
        return gameService.getState(gameId).then(function (gameState) {
            newState.selectedGameState = gameState
            return newState
        })
    } else if (action.leaveGame) {
        const oldEvents = state.eventSource
        if (oldEvents)
            oldEvents.close()
        const newState = Object.assign({}, state)
        delete newState.selectedGame
        delete newState.eventSource
        return Promise.resolve(newState)
    } else if (action.definePlayerName) {
        localStorage.setItem('playerName', action.definePlayerName)
        return Promise.resolve(Object.assign({}, state, {player: action.definePlayerName}))
        // game actions
    } else if (action.defineScenario)
        return lobbyService.defineGame(action.defineScenario)
            .then(r(state))
            .then(loadGamesFromBackend)
    else if (action.joinGame)
        return lobbyService.joinGame(action.joinGame, state.player)
            .then(r(state))
            .then(loadGamesFromBackend)
    else if (action.startGame)
        return lobbyService.startGame(action.startGame)
            .then(r(state))
            .then(loadGamesFromBackend)
    else if (action.defineAction) {
        if(!state.slots)
            state.slots = []
        var slot = action.defineAction.slot
        state.slots[slot] = {}
        state.slots[slot][action.defineAction.action] = {}
        if(_.range(constants.numberOfActionsPerCycle).every(function(i){return state.slots[i];}))
            gameService.defineAction(state.selectedGame, state.player, action.defineAction.cycle, state.slots)
        return Promise.resolve(state)
    } else {
        console.error("unknown action", action)
    }
}

function r(response) {
    return function () {
        return response
    }
}

function loadGamesFromBackend(state) {
    return lobbyService.getAllGames().then(function (games) {
        state.games = games
        return state
    })
}

module.exports = actions