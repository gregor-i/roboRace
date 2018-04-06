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
        return refreshGameFromBackend(oldState, action.enterGame).then(function(state){
            return Object.assign({}, state, {selectedGame: action.enterGame})
        })
    }else if(action.leaveGame){
        var newState = Object.assign({}, oldState)
        delete newState.selectedGame
        return Promise.resolve(newState)
    }
    else
        console.error("unknown action", action)
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
    return gameService.getState(id).then(function(game){
        state.games[id] = game
        return state
    })
}

module.exports = {
    apply: apply,
}
