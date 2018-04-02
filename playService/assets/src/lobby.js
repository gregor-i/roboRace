var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
])

var render = require('./lobby/index')
var service = require('./lobbyService')

function Lobby(element, player) {
    var init = function (player, games) {
        return {
            player: player,
            games: games,
        }
    }

    function updateCallback(action) {
        console.log("updateCallback(", action, ")")
    }

    /*var updateCallback = function (action) {
        var result = update(action, oldState)
        Result.case({
            Sync: function (newState) {
                newState.error = null
                main(newState, vnode)
            },
            Async: function (promise) {
                promise.then(function (newState) {
                    newState.error = null
                    main(newState, vnode)
                }).catch(function (err) {
                    oldState.error = err
                    main(oldState, vnode)
                })
            },
        }, result)
    }*/

    var node = element
    function main(oldState) {
        var vnode = render(oldState, updateCallback)
        node = patch(node, vnode)
    }

    service.getAllGames().then(function (games) {
        var state = init(player, games)
        main(state, element)
    })

    return this
}

document.addEventListener('DOMContentLoaded', function (event) {
    var container = document.getElementById('robo-rally-lobby')
    var player = "p1"
    new Lobby(container, player)
})
