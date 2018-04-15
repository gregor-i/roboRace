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

function updates(gameId) {
    return new EventSource("/api/games/" + gameId + "/events");
}

function parseJson(resp) {
    return resp.json()
}

module.exports = {
    getState: getState,
    sendCommand: sendCommand,
    defineAction: defineAction,
    updates: updates,
}
