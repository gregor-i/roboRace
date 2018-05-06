var h = require('snabbdom/h').default
var _ = require('lodash')
var constants = require('../constants')
var button = require('./button')

function render(state, game, actionHandler) {
    if (game.GameRunning) {
        var gameRunning = game.GameRunning
        return gameFrame('Game ' + state.selectedGame, [
                renderPlayerList(game.GameRunning.players),
                h('div.board', [
                    renderBoard(gameRunning),
                    renderRobots(gameRunning)
                ]),
                gameRunning.players.find(function(player){return player.name === state.player && !player.finished}) ?
                    renderActionButtons(state, gameRunning.cycle, gameRunning.robotActions, actionHandler) :
                    h('div', 'observer mode or target reached'),
                button.builder.disable(!state.animations)(actionHandler, {replayAnimations: state.animations}, 'Replay Animations')
            ],
            actionHandler)
    } else if (game.GameNotStarted) {
        return gameFrame('Game ' + state.selectedGame,
            [
                renderPlayerList(game.GameNotStarted.playerNames),
                button.group(
                    button.builder.primary()(actionHandler, {joinGame: state.selectedGame}, 'Join Game'),
                    button.builder.primary()(actionHandler, {startGame: state.selectedGame}, 'Start Game')
                )
            ], actionHandler)
    } else if (game.GameFinished) {
        return gameFrame('Game ' + state.selectedGame + ' is finished',
            renderPlayerList(game.GameFinished.players),
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

function renderPlayerList(players) {
    var rows = players.map(function (player, index) {
        return h('tr', [
            h('td', h('img', {
                props: {src: '/assets/gem' + (index + 1) + '.png'},
                style: {'max-width': '20px', 'max-height': '20px'}
            })),
            h('td', (index + 1)),
            h('td', player.name || player),
            h('td', player.finished ? player.finished.rank : '')
        ])
    })

    rows.unshift(h('tr', [h('th', 'icon'), h('th', 'index'), h('th', 'name'), h('th', 'finished as')]))

    return h('div', [
        h('h4', 'Players: '),
        h('table',rows)
    ])
}

function renderBoard(game) {
    return h('board', _.range(game.scenario.height).map(function (r) {
        return renderRow(game, r)
    }))
}

function renderRow(game, row) {
    return h('row', _.range(game.scenario.width).map(function (c) {
        return renderCell(game, row, c)
    }))
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

    const isBeacon = false //positionEqual(game.scenario.beaconPosition)
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
    var robots = game.players.map(function (player) {
        var robot = player.robot
        var x = robot.position.x * 50
        var y = robot.position.y * 50
        var rot = directionToRotation(robot.direction)
        return h('robot.robot' + (player.index % 6 + 1),
            {
                style: {
                    transform: 'translate(' + x + 'px, ' + y + 'px) rotate(' + rot + 'deg)',
                    opacity: player.finished ? '0' : '1',
                },
                props: {
                    title: JSON.stringify(player),
                    id: 'robot_' + (player.index % 6 + 1),
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
