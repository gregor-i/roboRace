import {headers, parseJson} from '../common/service-util'

export function getAllGames() {
  return fetch("/api/games", headers({}))
    .then(parseJson)
}

export function createGame(scenario, index) {
  return fetch("/api/games?index="+index, headers({
    method: "POST",
    body: JSON.stringify(scenario)
  })).then(parseJson)
}

export function deleteGame(gameId) {
  return fetch("/api/games/" + gameId, headers({method: "DELETE"}))
}

export function updates() {
  return new EventSource("/api/games/events")
}
