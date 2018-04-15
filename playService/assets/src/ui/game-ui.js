var h = require('snabbdom/h').default
var _ = require('lodash')
var constants = require('../constants')

function render(state, game, actionHandler) {
    if (game.GameRunning) {
        var gameRunning = game.GameRunning
        return gameFrame('Game ' + state.selectedGame, [
            h('div.board', [
                renderBoard(gameRunning),
                renderRobots(gameRunning)
            ]),
            renderActionButtons(state, gameRunning.cycle, gameRunning.robotActions, actionHandler)],
            actionHandler)
    } else if (game.GameNotStarted) {
        return gameFrame('Game '+state.selectedGame,
            [
                h('h3', 'joined Players:'),
                h('ol', game.GameNotStarted.playerNames.map(function(player){
                    return h('li', player)
                })),
                h('button.button.is-primary',
                    {on: {click: [actionHandler, {joinGame: state.selectedGame}]}},
                    'Join Game'),
                h('button.button.is-primary',
                    {on: {click: [actionHandler, {startGame: state.selectedGame}]}},
                    'Start Game')
            ], actionHandler)
    } else {
        return gameFrame('GameState ' + Object.keys(game)[0] + ' is currently not supported.', undefined, actionHandler)
    }
}

function gameFrame(title, content, actionHandler) {
    return h('div', [
        h('h1', title),
        h('div', content),
        h('button.button.is-primary',
            {on: {click: [actionHandler, {leaveGame: true}]}},
            'Back to Lobby')
    ])
}

function renderBoard(game) {
    var rows = _.range(game.scenario.height).map(function (r) {
        return renderRow(game, r)
    });
    return h('board', rows)
}

function renderRow(game, row) {
    var cells = _.range(game.scenario.width).map(function (c) {
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
    var robots = Object.keys(game.robots).map(function (player, index) {
        var robot = game.robots[player]
        var x = robot.position.x * 50
        var y = robot.position.y * 50
        var rot = 90 * directionToRotation(robot.direction)
        return h('robot.robot' + (index % 6 + 1),
            {
                style: {
                    transform: 'translate(' + x + 'px, ' + y + 'px) rotate(' + rot + 'deg)'
                },
                props: {title: player + " - " + Object.keys(robot.direction)[0]}
            });
    })
    return h('div', robots)
}

function directionToRotation(direction) {
    if (direction.Up)
        return 0
    else if (direction.Right)
        return 1
    else if (direction.Down)
        return 2
    else if (direction.Left)
        return 3
}

function renderActionButtons(state, cycle, robotActions, actionHandler) {
    var headerRow = h('tr', _.range(constants.numberOfActionsPerCycle).map(function (i) {
        return h('th', 'Action ' + (i + 1))
    }))

    function actionRow(action) {
        function actionButton(slot) {
            return h('td',
                h('button.button',
                    {
                        class: {'is-primary': _.get(state, ['slots', slot,  action])},
                        on: {click: [actionHandler, {defineAction: {player: state.player, cycle, slot, action}}]}
                    },
                    action))
        }

        return h('tr', _.range(constants.numberOfActionsPerCycle).map(actionButton))
    }

    return h('table', [
        headerRow,
        actionRow('MoveForward'),
        actionRow('TurnRight'),
        actionRow('TurnLeft'),
        actionRow('MoveBackward'),
    ])
}

module.exports = render