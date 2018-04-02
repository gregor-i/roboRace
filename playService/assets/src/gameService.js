function getState(gameId) {
    return fetch("/api/games/" + gameId)
        .then(parseJson)
}

function sendCommand(gameId, command) {
    return fetch("/api/games/" + game + "/commands", {
        method: "POST",
        body: JSON.stringify(command)
    })
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
    updates: updates,
}
