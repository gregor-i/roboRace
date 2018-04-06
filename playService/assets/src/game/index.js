var h = require('snabbdom/h').default

function render(state, game, update) {
    if (game.GameRunning) {
        var gameRunning = game.GameRunning
        return h('div', [
            h('h1', 'Game ' + state.selectedGame),
            h('div.board', [
                renderBoard(gameRunning),
                renderRobots(gameRunning)
            ]),
            h('button.button.is-primary',
                {on: {click: [update, state, {leaveGame: true}]}},
                'Back to Lobby')
        ])
    } else
        return h('div', h('h1', 'GameState ' + Object.keys(game)[0] + ' is currently not supported.'))
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
    var robots = Object.keys(game.robots).map(function (key, index) {
        var robot = game.robots[key]
        return h('robot.robot' + (index % 6 + 1),
            {
                style: {
                    transform: 'translate('+robot.position.x*50+'px, '+robot.position.y*50+'px) rotate('+90*directionToRotation(robot.direction)+'deg)'
                },
                props: {title: key}
            });
    })
    return h('div', robots)
}

function directionToRotation(direction) {
    if(direction.Up)
        return 0
    else if(direction.Right)
        return 1
    else if(direction.Down)
        return 2
    else if(direction.Left)
        return 3
}

function range(n) {
    return Array.apply(null, Array(n)).map(function (_, i) {
        return i;
    })
}

module.exports = render