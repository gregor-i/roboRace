var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
])

var render = require('./lobby/index')
var service = require('./lobbyService')
var actions = require('./lobbyActions')

function Lobby(element, player) {
    function state(player, games) {
        return {
            player: player,
            games: games,
        }
    }

    function updateCallback(oldState, action) {
        actions.apply(oldState, action)
            .then(function (newState) {
                console.log("state Transition", action, oldState, newState)
                main(newState, element)
            })
    }

    var node = element

    function main(oldState) {
        var vnode = render(oldState, updateCallback)
        node = patch(node, vnode)
    }

    service.getAllGames().then(function (games) {
        main(state(player, games), element)
    })

    return this
}

document.addEventListener('DOMContentLoaded', function (event) {
    var container = document.getElementById('robo-rally-lobby')
    var player = localStorage.getItem('playerName')
    new Lobby(container, player)
})
