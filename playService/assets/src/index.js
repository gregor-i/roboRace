var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
    require('snabbdom/modules/style').default
])

var renderLobby = require('./lobby/index')
var renderGame = require('./game/index')
var service = require('./lobby-service')
var actions = require('./actions')

function Lobby(element, player) {
    function updateCallback(oldState, action) {
        actions.apply(oldState, action)
            .then(function (newState) {
                console.log("state Transition", action, oldState, newState)
                renderState(newState, element)
            })
    }

    var node = element

    function renderState(state) {
        var vnode
        if(state.selectedGame && state.games[state.selectedGame])
            vnode = renderGame(state, state.games[state.selectedGame], updateCallback)
        else
            vnode = renderLobby(state, updateCallback)
        node = patch(node, vnode)
    }

    service.getAllGames().then(function (games) {
        renderState({
            player: player,
            games: games,
        }, element)
    })

    return this
}

document.addEventListener('DOMContentLoaded', function (event) {
    var container = document.getElementById('robo-race')
    var player = localStorage.getItem('playerName')
    new Lobby(container, player)
})
