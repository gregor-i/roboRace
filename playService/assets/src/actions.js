var lobbyService = require('./lobby-service')
var gameService = require('./game-service')

function apply(oldState, action){
    if(action.createGame)
        return lobbyService.createGame()
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.defineScenario)
        return lobbyService.defineGame(action.defineScenario)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.joinGame)
        return lobbyService.joinGame(action.joinGame, oldState.player)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.startGame)
        return lobbyService.startGame(action.startGame)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.deleteGame)
        return lobbyService.deleteGame(action.deleteGame)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.definePlayerName) {
        localStorage.setItem('playerName', action.definePlayerName)
        return Promise.resolve({player: action.definePlayerName, games: oldState.games})
    }else if(action.enterGame) {
        return refreshGameFromBackend(oldState, action.enterGame)().then(function(state){
            var oldEvents = state.eventSource
            if(oldEvents)
                oldEvents.close()
            var newEvents = gameService.updates(action.enterGame)
            newEvents.onmessage = function(x){
                console.log('Server Sent Event', x.data)
            }
            return Object.assign({}, state, {selectedGame: action.enterGame, eventSource: newEvents})
        })
    }else if(action.leaveGame){
        var newState = Object.assign({}, oldState)
        delete newState.selectedGame
        return Promise.resolve(newState)
    }else if(action.defineAction){
        var a = {}
        a[action.defineAction.action] = {}
        return gameService.defineAction(oldState.selectedGame, action.defineAction.player, action.defineAction.cycle, action.defineAction.slot, a)
            .then(refreshGameFromBackend(oldState,  oldState.selectedGame))
    }else {
        console.error("unknown action", action)
        return Promise.resolve(oldState)
    }
}

function debug(marker){
    return function(x){
        console.log(marker, x)
        return x;
    }
}

function r(response){
    return function(){
        return response
    }
}

function loadGamesFromBackend(state){
    return lobbyService.getAllGames().then(function(games){
        state.games = games
        return state
    })
}

function refreshGameFromBackend(state, id){
    return function() {
        return gameService.getState(id).then(function (game) {
            state.games[id] = game
            return state
        })
    }
}

module.exports = {
    apply: apply,
}
