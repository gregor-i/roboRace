import {headers, parseJson} from '../common/service-util'

export function loadAllScenarios() {
  return fetch('/api/scenarios', headers({}))
      .then(parseJson)
}

export function loadSingleScenario(id){
  return fetch('/api/scenarios/' + id, headers({}))
    .then(parseJson)
}

export function postScenario(description, scenario) {
  return fetch('/api/scenarios', headers({
    method: 'POST',
    body: JSON.stringify({description, scenario})
  })).then(parseJson)
}

export function deleteScenario(id) {
  return fetch('/api/scenarios/' + id, headers({
    method: 'DELETE'
  }))
}
