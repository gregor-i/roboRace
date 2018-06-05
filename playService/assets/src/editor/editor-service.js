const headers = require('../common/service-headers')

function loadAllScenarios() {
    return fetch("/api/scenarios", headers({}))
        .then(parseJson)
}

function postScenario(scenario) {
    return fetch("/api/scenarios", headers({
        method: "POST",
        body: JSON.stringify(scenario)
    }))
}

function parseJson(resp) {
    return resp.json()
}

module.exports = {
    loadAllScenarios,
    postScenario
}
