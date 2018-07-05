const headers = require('../common/service-headers')

function loadAllScenarios() {
  return fetch('/api/scenarios', headers({}))
      .then(parseJson)
}

function loadSingleScenario(id){
  return fetch('/api/scenarios/' + id, headers({}))
    .then(parseJson)
}

function postScenario(scenario) {
  return fetch('/api/scenarios', headers({
    method: 'POST',
    body: JSON.stringify(scenario)
  })).then(parseJson)
}

function deleteScenario(id) {
  return fetch('/api/scenarios/' + id, headers({
    method: 'DELETE'
  }))
}

function parseJson(resp) {
  return resp.json()
}

module.exports = {
  loadAllScenarios,
  loadSingleScenario,
  postScenario,
  deleteScenario
}
