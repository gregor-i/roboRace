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

    function updateCallback(action) {
        console.log("updateCallback(", action, ")")
        actions.apply(action)
            .then(function () {
                service.getAllGames().then(function (games) {
                    main(state(player, games), element)
                })
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
    var player = "p1"
    new Lobby(container, player)
})
