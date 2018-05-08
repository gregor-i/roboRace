var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
    require('snabbdom/modules/style').default
])

var lobbyUi = require('./lobby-ui')
var lobbyService = require('./lobby-service')
var actions = require('./lobby-actions')

function Lobby(element, player) {
    var node = element
    const lobbyUpdates = lobbyService.updates()

    function renderState(state) {
        lobbyUpdates.onmessage = lobbyEventHandler(state)
        window.currentState = state
        node = patch(node, lobbyUi(state, actionHandler(state)))
    }

    function actionHandler(state) {
        return function(action) {
            const promise = actions(state, action)
            if (promise && promise.then)
                promise.then(renderState)
        }
    }

    function lobbyEventHandler(state){
        return function(event){
            const data = JSON.parse(event.data)
            if(data.GameDeleted){
                delete state.games[data.GameDeleted.id]
                renderState(state)
            }else if(data.GameCreated){
                state.games[data.GameCreated.id] = data.GameCreated.state
                renderState(state)
            }else {
                console.error("unhandled lobby event", data)
            }
        }
    }

    lobbyService.getAllGames().then(function (games) {
        renderState({player, games}, element)
    })

    return this
}

module.exports = Lobby