import * as _ from 'lodash'
import {Game, ScenarioRow} from "./models";

const defaultHeader = {
  credentials: 'same-origin'
}

export function headers(additional) {
  return _.merge({}, defaultHeader, additional)
}


export function parseJson(resp): Promise<any> {
  if (resp.status - resp.status % 100 === 200)
    return resp.json()
  else {
    const data = resp.text()
    data.then(text => window.alert(`there was an unexpected problem with a service request. Response: (${resp.status}) ${text}`))
    return Promise.reject(data)
  }
}


export function getAllGames(): Promise<Game[]> {
  return <Promise<Game[]>>fetch("/api/games", headers({}))
    .then(parseJson)
}

export function createGame(scenario, index): Promise<Game> {
  return <Promise<Game>>fetch("/api/games?index=" + index, headers({
    method: "POST",
    body: JSON.stringify(scenario)
  }))
    .then(parseJson)
}

export function deleteGame(gameId): Promise<Response> {
  return fetch("/api/games/" + gameId, headers({method: "DELETE"}))
    .then(parseJson)
}

export function lobbyUpdates() {
  return new EventSource("/api/games/events")
}

export function getState(gameId): Promise<Game> {
  return <Promise<Game>>fetch("/api/games/" + gameId, headers({}))
    .then(parseJson)
}

function sendCommand(gameId, command): Promise<Game> {
  return <Promise<Game>>fetch("/api/games/" + gameId + "/commands", headers({
    method: "POST",
    body: JSON.stringify(command)
  }))
    .then(parseJson)
}

export function setInstruction(gameId, cycle, slot, instruction): Promise<Game> {
  return sendCommand(gameId, {SetInstruction: {cycle, slot, instruction}})
}

export function resetInstruction(gameId, cycle, slot): Promise<Game> {
  return sendCommand(gameId, {ResetInstruction: {cycle, slot}})
}

export function joinGame(gameId, index): Promise<Game> {
  return sendCommand(gameId, {RegisterForGame: {index}})
}

export function quitGame(gameId): Promise<Game> {
  return sendCommand(gameId, {DeregisterForGame: {}})
}

export function gameUpdates(gameId) {
  return new EventSource("/api/games/" + gameId + "/events")
}


export function loadAllScenarios(): Promise<ScenarioRow[]> {
  return <Promise<ScenarioRow[]>>fetch('/api/scenarios', headers({}))
    .then(parseJson)
}

export function loadSingleScenario(id): Promise<ScenarioRow> {
  return <Promise<ScenarioRow>>fetch('/api/scenarios/' + id, headers({}))
    .then(parseJson)
}

export function postScenario(description, scenario): Promise<ScenarioRow> {
  return <Promise<ScenarioRow>>fetch('/api/scenarios', headers({
    method: 'POST',
    body: JSON.stringify({description, scenario})
  }))
    .then(parseJson)
}

export function deleteScenario(id): Promise<Response> {
  return fetch('/api/scenarios/' + id, headers({
    method: 'DELETE'
  }))
}
