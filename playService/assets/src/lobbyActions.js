var service = require('./lobbyService')

function apply(action){
    if(action.createGame)
        return service.createGame()
    else if(action.defineScenario)
        return service.defineGame(action.defineScenario)
    else if(action.joinGame)
        return service.joinGame(action.joinGame, window.prompt("Player Name:", ""))
    else if(action.startGame)
        return service.startGame(action.startGame)
    else if(action.deleteGame)
        return service.deleteGame(action.deleteGame)
    else
        console.error("unknown action", action)
}


module.exports = {
    apply: apply,
}
