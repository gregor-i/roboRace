function getAllGames() {
  return fetch("/api/games")
      .then(parseJson)
}

function sendCommand(gameId, command) {
  return fetch("/api/games/" + game + "/commands", {
    method: "POST",
    body: JSON.stringify(command)
  })
}

function parseJson(resp) {
  return resp.json()
}

function createGame() {
  return fetch("/api/games",
      {method: "POST"})
}

function deleteGame(gameId) {
  return fetch("/api/games/" + gameId,
      {method: "DELETE"})
}

function joinGame(gameId, playerName) {
  return sendCommand(gameId, {RegisterForGame: {playerName: playerName}})
}

function defineGame(gameId) {
  return fetch("/default-scenario")
      .then(parseJson)
      .then(function (scenario) {
        sendCommand(gameId, {DefineScenario: {scenario: scenario}})
      })
}

module.exports = {
  getAllGames: getAllGames,
  sendCommand: sendCommand,
  createGame: createGame,
  deleteGame: deleteGame,
  joinGame: joinGame,
  defineGame: defineGame,
}
