var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
    require('snabbdom/modules/style').default
])

var ui = require('./ui/ui')
var service = require('./lobby-service')
var actions = require('./actions')
var animations = require('./ui/animations')

function Lobby(element, player) {
    var node = element
    const lobbyUpdates = service.updates()

    function renderState(state) {
        lobbyUpdates.onmessage = lobbyEventHandler(state)
        if(state.eventSource)
            state.eventSource.onmessage = gameEventHandler(state)

        window.currentState = state
        node = patch(node, ui(state, actionHandler(state)))
    }

    function actionHandler(state) {
        return function(action) {
            const promise = actions(state, action)
            if (promise && promise.then)
                promise.then(renderState)
        }
    }

    function gameEventHandler(state){
        return function(event){
            const data = JSON.parse(event.data)
            const newGameState = data.state
            const events = data.events
            state.animations = animations.animations(state.selectedGameState, newGameState, events)
            if(state.animations)
                animations.playAnimations(state.animations)
            state.selectedGameState = newGameState
            // console.log("game Events: ",events)
            if(events.find(function(event){
                return !! event.PlayerActionsExecuted
            })) state.slots = []
            renderState(state)
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
                console.log("unhandled lobby event", data)
            }
        }
    }

    service.getAllGames().then(function (games) {
        renderState({
            player: player,
            games: games,
            selectedGame: undefined,
            eventSource: undefined,
            selectedGameState: undefined,
            slots: []
        }, element)
    })

    return this
}

document.addEventListener('DOMContentLoaded', function (event) {
    var container = document.getElementById('robo-race')
    var player = localStorage.getItem('playerName')
    new Lobby(container, player)
})
