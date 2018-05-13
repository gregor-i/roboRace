function loadAllScenarios() {
    return fetch("/api/scenarios")
        .then(parseJson)
}

function postScenario(scenario) {
    return fetch("/api/scenarios", {
        method: "POST",
        body: JSON.stringify(scenario)
    })
}

function parseJson(resp) {
    return resp.json()
}

module.exports = {
    loadAllScenarios,
    postScenario
}
