var service = require('./lobby-service')

function apply(oldState, action){
    if(action.createGame)
        return service.createGame()
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.defineScenario)
        return service.defineGame(action.defineScenario)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.joinGame)
        return service.joinGame(action.joinGame, oldState.player)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.startGame)
        return service.startGame(action.startGame)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.deleteGame)
        return service.deleteGame(action.deleteGame)
            .then(r(oldState))
            .then(loadGamesFromBackend)
    else if(action.definePlayerName) {
        localStorage.setItem('playerName', action.definePlayerName)
        return Promise.resolve({player: action.definePlayerName, games: oldState.games})
    }else
        console.error("unknown action", action)
}

function r(response){
    return function(){
        return response
    }
}

function loadGamesFromBackend(state){
    return service.getAllGames().then(function(games){
        state.games = games
        return state
    })
}


module.exports = {
    apply: apply,
}
