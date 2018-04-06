function getAllGames() {
  return fetch("/api/games")
      .then(parseJson)
}

function sendCommand(gameId, command) {
  return fetch("/api/games/" + gameId + "/commands", {
    method: "POST",
    body: JSON.stringify(command)
  })
}

function createGame() {
  return fetch("/api/games", {method: "POST"})
}

function deleteGame(gameId) {
  return fetch("/api/games/" + gameId, {method: "DELETE"})
}

function parseJson(resp) {
    return resp.json()
}

function joinGame(gameId, playerName) {
  return sendCommand(gameId, {RegisterForGame: {playerName: playerName}})
}

function startGame(gameId){
  return sendCommand(gameId, {StartGame: {}})
}

function defineGame(gameId) {
  return fetch("/default-scenario")
      .then(parseJson)
      .then(function (scenario) {
        return sendCommand(gameId, {DefineScenario: {scenario: scenario}})
      })
}

module.exports = {
  getAllGames: getAllGames,
  createGame: createGame,
  deleteGame: deleteGame,
  joinGame: joinGame,
  defineGame: defineGame,
  startGame: startGame,
}
