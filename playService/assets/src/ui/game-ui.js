var h = require('snabbdom/h').default
var _ = require('lodash')
var constants = require('../constants')
var button = require('./button')

function render(state, game, actionHandler) {
    if (game.GameRunning) {
        var gameRunning = game.GameRunning
        return gameFrame('Game ' + state.selectedGame, [
                h('div.board', [
                    renderBoard(gameRunning),
                    renderRobots(gameRunning)
                ]),
                gameRunning.players.includes(state.player) ?
                    renderActionButtons(state, gameRunning.cycle, gameRunning.robotActions, actionHandler) :
                    'Observer mode',
                button.builder.disable(!state.animations)(actionHandler, {replayAnimations: state.animations}, 'Replay Animations')
            ],
            actionHandler)
    } else if (game.GameNotStarted) {
        return gameFrame('Game ' + state.selectedGame,
            [
                renderPlayerList('joined Players:', game.GameNotStarted.playerNames),
                button.group(
                    button.builder.primary()(actionHandler, {joinGame: state.selectedGame}, 'Join Game'),
                    button.builder.primary()(actionHandler, {startGame: state.selectedGame}, 'Start Game')
                )
            ], actionHandler)
    } else if (game.GameFinished) {
        return gameFrame('Game ' + state.selectedGame + ' is finished',
            renderPlayerList('finial ranking:', game.GameFinished.players.map(function(obj){return obj.playerName})),
            actionHandler)
    } else {
        return gameFrame('GameState ' + Object.keys(game)[0] + ' is currently not supported.', undefined, actionHandler)
    }
}

function gameFrame(title, content, actionHandler) {
    return h('div', [
        h('h1', title),
        h('div', content),
        button.builder.primary()(actionHandler, {leaveGame: true}, 'Back to Lobby')
    ])
}

function renderPlayerList(title, playerNames) {
    return h('h4', [
        title,
        h('ol',
            playerNames.map(function (playerName) {
                    return h('li', playerName)
                }
            ))]
    )
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

    const isWallRight = !! game.scenario.walls.find(function(wall){
        return wall.direction.Right && positionEqual(wall.position)
    })
    const isWallDown = !! game.scenario.walls.find(function(wall){
        return wall.direction.Down && positionEqual(wall.position)
    })
    const isPit = !!game.scenario.pits.find(function(pit){
        return positionEqual(pit)
    })

    const isBeacon = positionEqual(game.scenario.beaconPosition)
    const isTarget = positionEqual(game.scenario.targetPosition)

    var desc
    if(isTarget)
        desc = 'Target'
    else if(isBeacon)
        desc = 'Beacon'
    else if(isPit)
        desc = 'Pit'
    else
        desc = 'normal Field'

    return h('cell', {
        class: {
            'wall-down': isWallDown,
            'wall-right': isWallRight,
            'beacon-cell': isBeacon,
            'target-cell': isTarget,
            'pit' : isPit
        },
        props: {title: `${column}, ${row} => ${desc}`}
    }, '')
}


function renderRobots(game) {
    var robots = Object.keys(game.robots).map(function (player, index) {
        var robot = game.robots[player]
        var x = robot.position.x * 50
        var y = robot.position.y * 50
        var rot = directionToRotation(robot.direction)
        return h('robot.robot' + (index % 6 + 1),
            {
                style: {
                    transform: 'translate(' + x + 'px, ' + y + 'px) rotate(' + rot + 'deg)',
                    opacity: robot.finished ? '0' : '1',
                },
                props: {
                    title: player + " - " + Object.keys(robot.direction)[0],
                    id: 'robot_' + (index % 6 + 1),
                    x: x,
                    y: y,
                    rot: rot + ''
                }
            });
    })
    return h('div', robots)
}

function directionToRotation(direction) {
    if (direction.Up)
        return "0"
    else if (direction.Right)
        return "90"
    else if (direction.Down)
        return "180"
    else if (direction.Left)
        return "270"
    else
        console.error("unkown direction", direction)
}

function renderActionButtons(state, cycle, robotActions, actionHandler) {
    var headerRow = h('tr', _.range(constants.numberOfActionsPerCycle).map(function (i) {
        return h('th', 'Action ' + (i + 1))
    }))

    function actionRow(action) {
        function actionButton(slot) {
            var isEnabled = !!(_.get(state, ['slots', slot, action]))
            return h('td',
                    button.builder.primary(isEnabled)(actionHandler,
                        {defineAction: {player: state.player, cycle, slot, action}},
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