function getState(gameId) {
    return fetch("/api/games/" + gameId)
        .then(parseJson)
}

function sendCommand(gameId, command) {
    return fetch("/api/games/" + gameId + "/commands", {
        method: "POST",
        body: JSON.stringify(command)
    })
}

function defineAction(gameId, player, cycle, actions) {
    return sendCommand(gameId, {DefineNextAction: {player, cycle, actions}})
        .then(parseJson)
}

function joinGame(gameId, playerName) {
    return sendCommand(gameId, {RegisterForGame: {playerName}})
}

function readyForGame(gameId, playerName){
    return sendCommand(gameId, {ReadyForGame: {playerName}})
}

function defineScenario(gameId, scenario) {
    return sendCommand(gameId, {DefineScenario: {scenario: scenario}})
}

function updates(gameId) {
    return new EventSource("/api/games/" + gameId + "/events")
}

function parseJson(resp) {
    return resp.json()
}

module.exports = {
    getState,
    defineAction,
    defineScenario,
    readyForGame,
    joinGame,
    updates
}
