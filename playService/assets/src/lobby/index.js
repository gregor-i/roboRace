var h = require('snabbdom/h').default

function render(state, update) {
    return h('div', [
            renderLoginModal(state, state.player, update),
            h('h1', 'Game Lobby:'),
            renderGameTable(state, state.games, update),
            h('button.button.is-primary',
                {on: {click: [update, state, {createGame: true}]}},
                'New Game')
        ]
    )
}

function renderGameTable(state, games, update) {
    var rows = Object.keys(games).map(function (id) {
        return renderGameRow(state, id, games[id], update)
    })

    var header = h('tr', [
        h('th', 'Id'),
        h('th', 'State'),
        h('th', 'Players'),
        h('th', 'Actions'),
    ])

    rows.unshift(header)

    return h('table', rows)
}

function renderGameRow(feState, id, game, update) {
    var state = Object.keys(game)[0]
    var players
    if (state === 'GameRunning')
        players = game[state].players
    else if (state === 'GameNotStarted')
        players = game[state].playerNames
    else
        players = []

    var actions = [
        state === 'GameNotDefined' ?
            h('button.button.is-primary',
                {on: {click: [update, feState, {defineScenario: id}]}},
                'Define Scenario') : undefined,
        state === 'GameNotStarted' ?
            h('button.button.is-primary',
                {on: {click: [update, feState, {joinGame: id}]}},
                'Join') : undefined,
        state === 'GameNotStarted' ?
            h('button.button.is-primary',
                {on: {click: [update, feState, {startGame: id}]}},
                'Start') : undefined,
        state === 'GameRunning' ?
            h('button.button.is-primary',
                {on: {click: [update, feState, {enterGame: id}]}},
                'Enter') : undefined,
        h('button.button.is-danger',
            {on: {click: [update, feState, {deleteGame: id}]}},
            'Delete')
    ]

    return h('tr', [
        h('td', id),
        h('td', state),
        h('td', h('span.tags',
            players.map(function (player) {
                return h('tag.tag', player)
            }))),
        h('td', h('span.buttons', actions))
    ])
}

function renderLoginModal(state, player, update) {
    function submit() {
        var player = document.getElementById('player-name-input').value
        if(player)
            update(state, {definePlayerName: player})
    }

    if (!player) {
        console.log("modal")
        return h('div.modal.is-active', [
            h('div.modal-background'),
            h('div.modal-content', [
                h('div.box.column.is-4.is-offset-4', [
                    h('h3', 'Login'),
                    h('input.input.is-primary', {
                            props: {placeholder: 'Name',
                            id: 'player-name-input'},
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