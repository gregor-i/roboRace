function getAllGames() {
  return fetch("/api/games")
      .then(parseJson)
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

function updates() {
    return new EventSource("/api/games/events");
}

module.exports = {
  getAllGames,
  createGame,
  deleteGame,
  updates
}
