var h = require('snabbdom/h').default
var button = require('../common/button')
var modal = require('../common/modal')
var frame = require('../common/frame')

function render(state, actionHandler) {
    return frame([h('h1', 'Game Lobby:'), button.group(
        button.builder.primary()(actionHandler, {createGame: true}, 'New Game'),
        button.builder(actionHandler, {reloadGameList: true}, 'Refresh'),
        button.builder(actionHandler, {resetUserName: true}, 'Logout')
        )],
        h('div.content', renderGameTable(state, state.games, actionHandler)),
        undefined,
        renderLoginModal(state.player, actionHandler)
    )
}

function renderGameTable(state, games, actionHandler) {
    var rows = Object.keys(games).map(function (id) {
        return renderGameRow(id, games[id], actionHandler)
    })

    var header = h('tr', [
        h('th', 'Id'),
        h('th', 'State'),
        h('th', 'Actions'),
    ])

    rows.unshift(header)

    return h('table', rows)
}

function renderGameRow(id, gameState, actionHandler) {
    return h('tr', [
        h('td', id),
        h('td', gameState),
        h('td', button.group(
            button.builder.primary()(actionHandler, {enterGame: id}, 'Enter'),
            button.builder(actionHandler, {deleteGame: id}, 'Delete')
        ))
    ])
}

function renderLoginModal(player, actionHandler) {
    function submit() {
        var player = document.getElementById('player-name-input').value
        if (player)
            actionHandler({definePlayerName: player})
    }

    if (!player) {
        return modal([
            h('h3', 'Login'),
            h('input.input.is-primary', {
                    props: {
                        placeholder: 'Name',
                        id: 'player-name-input'
                    },
                    on: {
                        keydown: function (event) {
                            if (event.key === 'Enter')
                                submit()
                        }
                    }
                }
            ),
            h('a.button.is-primary', {on: {click: submit}}, 'Enter')
        ])
    } else {
        return undefined
    }
}

module.exports = render