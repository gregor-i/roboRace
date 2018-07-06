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

function setInstruction(gameId, cycle, slot, instruction) {
    return sendCommand(gameId, {SetInstruction: {cycle, slot, instruction}})
        .then(parseJson)
}

function resetInstruction(gameId, cycle, slot){
  return sendCommand(gameId, {ResetInstruction: {cycle, slot}})
      .then(parseJson)
}

function joinGame(gameId) {
    return sendCommand(gameId, {RegisterForGame: {}})
        .then(parseJson)
}

function updates(gameId) {
    return new EventSource("/api/games/" + gameId + "/events")
}

function parseJson(resp) {
    return resp.json()
}

module.exports = {
    getState,
    setInstruction,
    resetInstruction,
    joinGame,
    updates
}
