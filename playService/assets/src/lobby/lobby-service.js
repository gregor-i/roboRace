const headers = require('../common/service-headers')

function getAllGames() {
  return fetch("/api/games", headers({}))
    .then(parseJson)
}

function createGame() {
  return fetch("/api/games", headers({method: "POST"}))
}

function deleteGame(gameId) {
  return fetch("/api/games/" + gameId, headers({method: "DELETE"}))
}

function parseJson(resp) {
  return resp.json()
}

function updates() {
  return new EventSource("/api/games/events")
}

module.exports = {
  getAllGames,
  createGame,
  deleteGame,
  updates
}
