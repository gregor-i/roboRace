var h = require('snabbdom/h').default

function render(state, update) {
    if (state.selectedGame && state.games[state.selectedGame] && state.games[state.selectedGame].GameRunning) {
        var selectedGame = state.games[state.selectedGame].GameRunning
        return h('div', [
            h('h1', 'Game ' + state.selectedGame),
            renderBoard(selectedGame),
            renderRobots(selectedGame),
            h('button.button.is-primary',
                {on: {click: [update, state, {leaveGame: true}]}},
                'Back to Lobby')
        ])
    } else
        return h('div', h('h1', 'Game is not running'))
}

function renderBoard(game) {
    var rows = range(game.scenario.height).map(function (r) {
        return renderRow(game, r)
    });
    return h('board', rows)
}

function renderRow(game, row) {
    var cells = range(game.scenario.width).map(function (c) {
        return renderCell(game, row, c)
    })
    return h('row', cells)
}

function renderCell(game, row, column) {
    function positionEqual(pos) {
        return pos.x === column && pos.y === row
    }

    var isWallRight = false
    var isWallDown = false
    game.scenario.walls.forEach(function (wall) {
        if (positionEqual(wall.position)) {
            if (wall.direction.Down) isWallDown = true
            if (wall.direction.Right) isWallRight = true
        }
    })

    var isBeacon = positionEqual(game.scenario.beaconPosition)
    var isTarget = positionEqual(game.scenario.targetPosition)

    return h('cell', {
        class: {
            'wall-down': isWallDown,
            'wall-right': isWallRight,
            'beacon-cell': isBeacon,
            'target-cell': isTarget
        },
        props: {title: row + ", " + column}
    }, '')
}


function renderRobots(game) {
    var robots = Object.keys(game.robots).map(function(key, index) {
        var robot = game.robots[key]
        return h('robot', {props:{title:JSON.stringify(robot)}});
    })
    return h('div', robots)
}


function range(n) {
    return Array.apply(null, Array(n)).map(function (_, i) {
        return i;
    })
}

module.exports = render