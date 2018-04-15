var gameUi = require("./game-ui")
var lobbyUi = require("./lobby-ui")

function ui(state, actionHandler) {
    if (state.selectedGame && state.selectedGameState)
        return gameUi(state, state.selectedGameState, actionHandler)
    else
        return lobbyUi(state, actionHandler)
}

module.exports = ui