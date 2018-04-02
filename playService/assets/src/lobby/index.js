var h = require('snabbdom/h').default

function render(model, update) {
    return h('div', [
            h('h1', 'Game Lobby:'),
            renderGameTable(model.games, update),
            h('button.button.is-primary',
                {on: {click: [update, {createGame: true}]}},
                'New Game')
        ]
    )
}

function renderGameTable(games, update) {
    var rows = Object.keys(games).map(function (id) {
        return renderGameRow(id, games[id], update)
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

function renderGameRow(id, game, update) {
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
                {on: {click: [update, {defineScenario: id}]}},
                'Define Scenario') : undefined,
        state === 'GameNotStarted' ?
            h('button.button.is-primary',
                {on: {click: [update, {joinGame: id}]}},
                'Join') : undefined,
        state === 'GameNotStarted' ?
            h('button.button.is-primary',
                {on: {click: [update, {startGame: id}]}},
                'Start') : undefined,
        h('button.button.is-danger',
            {on: {click: [update, {deleteGame: id}]}},
            'Delete')
    ]

    return h('tr', [
        h('td', id),
        h('td', state),
        h('td', h('span.buttons',
            players.map(function (player) {
                return h('button.button.is-small.is-link.is-outlined',
                    {on: {click: [update, {enterGame: id}]},},
                    player)
            }))),
        h('td', h('span.buttons', actions))
    ])
}

module.exports = render