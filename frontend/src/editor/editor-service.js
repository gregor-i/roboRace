const {headers, parseJson} = require('../common/service-util')

function loadAllScenarios() {
  return fetch('/api/scenarios', headers({}))
      .then(parseJson)
}

function loadSingleScenario(id){
  return fetch('/api/scenarios/' + id, headers({}))
    .then(parseJson)
}

function postScenario(description, scenario) {
  return fetch('/api/scenarios', headers({
    method: 'POST',
    body: JSON.stringify({description, scenario})
  })).then(parseJson)
}

function deleteScenario(id) {
  return fetch('/api/scenarios/' + id, headers({
    method: 'DELETE'
  }))
}

module.exports = {
  loadAllScenarios,
  loadSingleScenario,
  postScenario,
  deleteScenario
}
