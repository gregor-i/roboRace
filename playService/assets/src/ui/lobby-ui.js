var h = require('snabbdom/h').default
var button = require('./button')

function render(state, actionHandler) {
    return h('div', [
            renderLoginModal(state.player, actionHandler),
            h('h1', 'Game Lobby:'),
            renderGameTable(state, state.games, actionHandler),
            button.group(
                button.builder.primary()(actionHandler, {createGame: true}, 'New Game'),
                button.builder.info()(actionHandler, {reloadGameList: true}, 'Reload')
            )
        ]
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
            button.builder.danger()(actionHandler, {deleteGame: id}, 'Delete')
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
        return h('div.modal.is-active', [
            h('div.modal-background'),
            h('div.modal-content', [
                h('div.box.column.is-4.is-offset-4', [
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
            ]),
            h('button.modal-close.is-large')
        ])
    } else {
        return undefined
    }
}

module.exports = render