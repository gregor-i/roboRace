var _ = require('lodash')
var h = require('snabbdom/h').default
var constants = require('../common/constants')
var button = require('../common/button')
var modal = require('../common/modal')

function render(state, actionHandler) {
    var m = null
    if(state.modal === 'log')
        m = modal(renderLog(state.logs), [actionHandler, {setModal: 'none'}])
    else if(state.modal === 'playerList')
        m = modal(renderPlayerList(state), [actionHandler, {setModal: 'none'}])

    if (state.game.GameNotStarted) {
        const game = state.game.GameNotStarted
        return gameFrame('Game ' + state.gameId,
            m,
            renderPlayerList(state),
            undefined,
            [backToLobbyButton(actionHandler), joinGameButton(state, game, actionHandler), startGameButton(state.gameId, actionHandler)])
    } else if (state.game.GameRunning || state.game.GameFinished) {
        const game = state.game.GameRunning || state.game.GameFinished
        return gameFrame('Game ' + state.gameId,
            m,
            h('div.board', [
                renderBoard(game.scenario),
                renderRobots(game.players)
            ]),
            renderActionButtons(state, game, actionHandler),
            [
                backToLobbyButton(actionHandler),
                animationsButton(state.animations, actionHandler),
                logsButton(actionHandler),
                playListButton(actionHandler)
            ])
    } else {
        return gameFrame('GameState \'undefined\' is currently not supported.', m, undefined, undefined, [backToLobbyButton(actionHandler)])
    }
}

function logsButton(actionHandler){
    return button.builder(actionHandler, {setModal: 'log'}, 'Logs')
}

function playListButton(actionHandler){
    return button.builder(actionHandler, {setModal: 'playerList'}, 'Player List')
}

function backToLobbyButton(actionHandler) {
    return button.link(actionHandler, {leaveGame: true}, 'Back to Lobby')
}

function animationsButton(animations, actionHandler){
    return button.builder.disable(!animations || animations.length === 0)(actionHandler, {replayAnimations: animations}, 'Replay Animations')
}

function joinGameButton(state, game, actionHandler){
    return button.builder.primary().disable(game.playerNames.includes(state.player))(actionHandler, {joinGame: state.gameId}, 'Join Game')
}

function startGameButton(gameId, actionHandler){
    return button.primary(actionHandler, {startGame: gameId}, 'Start Game')
}

function gameFrame(title, modal, body, footer, headerButtons) {
    return h('div.frame', [
        modal,
        h('div.content.frame-header', [h('h1', title), button.group(headerButtons)]),
        h('div.content.frame-body', body),
        h('div.content.frame-footer', footer)
    ])
}

function renderPlayerList(state) {
    var players
    if (state.game.GameNotStarted)
        players = state.game.GameNotStarted.playerNames
    else if(state.game.GameRunning)
        players = state.game.GameRunning.players
    else if(state.game.GameFinished)
        players = state.game.GameFinished.players

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

function renderBoard(scenario) {
    return h('board', _.range(scenario.height).map(function (r) {
        return renderRow(scenario, r)
    }))
}

function renderRow(scenario, row) {
    return h('row', _.range(scenario.width).map(function (c) {
        return renderCell(scenario, row, c)
    }))
}

function renderCell(scenario, row, column) {
    function positionEqual(pos) {
        return pos.x === column && pos.y === row
    }

    const isWallRight = !!scenario.walls.find(function (wall) {
        return wall.direction.Right && positionEqual(wall.position)
    })
    const isWallDown = !!scenario.walls.find(function (wall) {
        return wall.direction.Down && positionEqual(wall.position)
    })
    const isPit = !!scenario.pits.find(function (pit) {
        return positionEqual(pit)
    })

    const isBeacon = false //positionEqual(scenario.beaconPosition)
    const isTarget = positionEqual(scenario.targetPosition)

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


function renderRobots(players) {
    return h('div', players.map(function (player) {
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
    }))
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

function renderActionButtons(state, game, actionHandler) {
    const player = game.players.find(function (player) {
        return player.name === state.player
    })
    const slots = state.slots

    const text = !player ? 'observer mode' : (player.finished ? 'target reached' : 'action bar')

    function actionSelect(slot) {
        const options = player ? player.possibleActions.map((action, index) =>
            h('option',
                {
                    props: {
                        selected: slots[slot] === index,
                        disabled: slots.includes(index) && slots[slot] !== index
                    },
                    class: {selectedOption: slots[slot] === index}
                },
                Object.keys(action)[0])) : []


        options.unshift(h('option', 'unselected'))
        return h('span',
            h('select',
                {
                    props:{
                      disabled : !player || player.finished
                    },
                    class: {
                        selectedOption: slots[slot] !== undefined && slots[slot] !== -1
                    },
                    on: {
                        change: function (event) {
                            actionHandler({defineAction: {value: event.target.selectedIndex - 1, slot, cycle: game.cycle}})
                        }
                    }
                },
                options
            )
        )
    }

    return h('div.control-panel', [h('div', text), h('div', _.range(constants.numberOfActionsPerCycle).map(actionSelect))])
}

function renderLog(logs) {
    return h('div', [
        h('h4', 'Log: '),
        h('div', logs && logs.length ? logs.map(log => h('div', JSON.stringify(log))) : [])
    ])
}

module.exports = render
