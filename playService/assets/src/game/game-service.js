const headers = require('../common/service-headers')

function getState(gameId) {
    return fetch("/api/games/" + gameId, headers({}))
        .then(parseJson)
}

function sendCommand(gameId, command) {
    return fetch("/api/games/" + gameId + "/commands", headers({
        method: "POST",
        body: JSON.stringify(command)
    }))
}

function defineInstruction(gameId, player, cycle, instructions) {
    return sendCommand(gameId, {ChooseInstructions: {player, cycle, instructions}})
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
    defineInstruction,
    defineScenario,
    readyForGame,
    joinGame,
    updates
}
