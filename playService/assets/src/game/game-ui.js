var _ = require('lodash')
var h = require('snabbdom/h').default
var constants = require('../common/constants')
var button = require('../common/button')

function render(state, actionHandler) {
    var game = state.game
    if (game.GameNotStarted) {
        game = game.GameNotStarted
        return gameFrame('Game ' + state.gameId,
            [
                renderPlayerList(game.playerNames),
                button.group(
                    button.builder.primary().disable(game.playerNames.includes(state.player))(actionHandler, {joinGame: state.gameId}, 'Join Game'),
                    button.primary(actionHandler, {startGame: state.gameId}, 'Start Game')
                )
            ], actionHandler)
    } else if (game.GameRunning || game.GameFinished) {
        game = game.GameRunning || game.GameFinished
        const player = game.players.find(function (player) {
            return player.name === state.player
        })
        return gameFrame('Game ' + state.gameId, [
                renderPlayerList(game.players),
                h('div.board', [
                    renderBoard(game),
                    renderRobots(game)
                ]),
                !player ? h('div', 'observer mode') :
                    (player.finished ? h('div', 'target reached') :
                        renderActionButtons(state, game.cycle, player, actionHandler)),
                button.builder.disable(!state.animations || state.animations.length === 0)(actionHandler, {replayAnimations: state.animations}, 'Replay Animations')
            ],
            actionHandler)
    } else {
        return gameFrame('GameState \'undefined\' is currently not supported.', undefined, actionHandler)
    }
}

function gameFrame(title, content, actionHandler) {
    return h('div', [
        h('h1', title),
        h('div', content),
        button.primary(actionHandler, {leaveGame: true}, 'Back to Lobby')
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
        h('table', rows)
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

    const isWallRight = !!game.scenario.walls.find(function (wall) {
        return wall.direction.Right && positionEqual(wall.position)
    })
    const isWallDown = !!game.scenario.walls.find(function (wall) {
        return wall.direction.Down && positionEqual(wall.position)
    })
    const isPit = !!game.scenario.pits.find(function (pit) {
        return positionEqual(pit)
    })

    const isBeacon = false //positionEqual(game.scenario.beaconPosition)
    const isTarget = positionEqual(game.scenario.targetPosition)

    var desc
    if (isTarget)
        desc = 'Target'
    else if (isBeacon)
        desc = 'Beacon'
    else if (isPit)
        desc = 'Pit'
    else
        desc = 'normal Field'

    return h('cell', {
        class: {
            'wall-down': isWallDown,
            'wall-right': isWallRight,
            'beacon-cell': isBeacon,
            'target-cell': isTarget,
            'pit': isPit
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

function renderActionButtons(state, cycle, player, actionHandler) {
    function actionSelect(slot) {
        const options = player.possibleActions.map((action, index) =>
            h('option',
                {
                    props: {
                        selected: state.slots[slot] === index,
                        disabled: state.slots.includes(index) && state.slots[slot] !== index
                    },
                    style: {'font-weight': state.slots[slot] === index ? 'bold' : ''}
                },
                Object.keys(action)[0]))
        options.unshift(h('option', 'unselected'))
        return h('span.control',
            h('select.select',
                {
                    class: {'is-primary': !(state.slots[slot] > 0)},
                    on: {
                        change: function (event) {
                            actionHandler({defineAction: {value: event.target.selectedIndex - 1, slot, cycle}})
                        }
                    }
                },
                options
            )
        )
    }

    var options = _.range(constants.numberOfActionsPerCycle).map(actionSelect)
    options.unshift(h('h4', 'Actions: '))
    return h('div', options)
}

module.exports = render
