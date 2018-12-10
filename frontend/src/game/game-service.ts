import {headers, parseJson} from '../common/service-util'

export function getState(gameId) {
    return fetch("/api/games/" + gameId, headers({}))
        .then(parseJson)
}

export function sendCommand(gameId, command) {
    return fetch("/api/games/" + gameId + "/commands", headers({
        method: "POST",
        body: JSON.stringify(command)
    }))
}

export function setInstruction(gameId, cycle, slot, instruction) {
    return sendCommand(gameId, {SetInstruction: {cycle, slot, instruction}})
        .then(parseJson)
}

export function resetInstruction(gameId, cycle, slot){
  return sendCommand(gameId, {ResetInstruction: {cycle, slot}})
      .then(parseJson)
}

export function joinGame(gameId, index) {
    return sendCommand(gameId, {RegisterForGame: {index}})
        .then(parseJson)
}

export function quitGame(gameId){
  return sendCommand(gameId, {DeregisterForGame: {}})
      .then(parseJson)
}

export function updates(gameId) {
    return new EventSource("/api/games/" + gameId + "/events")
}
