function reload() {
    document.location.reload()
}

function newGame(){
    fetch("/api/games",{
        method: "POST"
    }).then(reload)
}

function deleteGame(game){
    fetch("/api/games/"+game,{
        method: "DELETE"
    }).then(reload)
}

function sendCommand(game, command) {
    return fetch("/api/games/"+game+"/commands", {
        method: "POST",
        body: JSON.stringify(command)
    })
}

function joinGame(game){
    var playerName = prompt("enter player name:", "")
    sendCommand(game, {"RegisterForGame" : {"playerName": playerName}})
        .then(reload)
}

function startGame(game) {
    sendCommand(game, {"StartGame": {}}).then(reload)
}

function defineGame(game){
    fetch("/default-scenario")
        .then(resp => resp.json())
        .then(scenario => sendCommand(game, {DefineScenario: { scenario: scenario}}))
        .then(reload)
}
