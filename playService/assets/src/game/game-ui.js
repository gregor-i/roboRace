var _ = require('lodash')
var h = require('snabbdom/h').default
var constants = require('../common/constants')
var button = require('../common/button')
var modal = require('../common/modal')
var frame = require('../common/frame')

function render(state, actionHandler) {
    var m = null
    if (state.modal === 'log')
        m = modal(renderLog(state.logs), [actionHandler, {setModal: 'none'}])
    else if (state.modal === 'playerList')
        m = modal(renderPlayerList(state), [actionHandler, {setModal: 'none'}])

    if(state.game.InitialGame) {
        const game = state.game.InitialGame
        return frame(header('Initial Game', [
                backToLobbyButton(actionHandler),
            ]),
            Object.keys(state.scenarios).map(key => renderScenarioPreview(key, state.scenarios[key], actionHandler)),
            undefined,
            m)
    }else if (state.game.GameStarting) {
        const game = state.game.GameStarting
        return frame(header('Game ' + state.gameId, [
                backToLobbyButton(actionHandler),
                joinGameButton(state, game, actionHandler),
                readyButton(state, game, actionHandler)
            ]),
            renderPlayerList(state),
            undefined,
            m)
    } else if (state.game.GameRunning || state.game.GameFinished) {
        const game = state.game.GameRunning || state.game.GameFinished
        return frame(header('Game ' + state.gameId, [
                backToLobbyButton(actionHandler),
                animationsButton(state.animations, actionHandler),
                logsButton(actionHandler),
                playerListButton(actionHandler)
            ]),
            h('div.board', [
                renderBoard(game.scenario),
                renderPlayerRobots(game.players)
            ]),
            renderActionButtons(state, game, actionHandler),
            m)
    } else {
        return frame(header('GameState \'undefined\' is currently not supported.', [
                backToLobbyButton(actionHandler),
            ]),
            undefined,
            undefined,
            m)
    }
}

function header(title, buttons){
    return button.group(buttons)
}

function logsButton(actionHandler) {
    return button.builder(actionHandler, {setModal: 'log'}, 'Logs')
}

function playerListButton(actionHandler) {
    return button.builder(actionHandler, {setModal: 'playerList'}, 'Player List')
}

function backToLobbyButton(actionHandler) {
    return button.link(actionHandler, {leaveGame: true}, 'Back to Lobby')
}

function animationsButton(animations, actionHandler) {
    return button.builder.disable(!animations || animations.length === 0)(actionHandler, {replayAnimations: animations}, 'Replay Animations')
}

function joinGameButton(state, game, actionHandler) {
    return button.builder.primary().disable(!!game.players.find(player => player.name === state.player))(actionHandler, {joinGame: state.gameId}, 'Join Game')
}

function readyButton(state, game, actionHandler) {
    return button.builder.disable(_.get(game.players.find(player => player.name === state.player), 'ready')).primary()(actionHandler, {readyForGame: state.gameId}, 'Ready')
}

function renderPlayerList(state) {
    var players = []
    if (state.game.GameStarting)
        players = state.game.GameStarting.players
    else if (state.game.GameRunning)
        players = state.game.GameRunning.players
    else if (state.game.GameFinished)
        players = state.game.GameFinished.players

    var rows = players.map(function (player, index) {
        return h('tr', [
            h('td', h('img', {
                props: {src: '/assets/gem' + (index + 1) + '.png'},
                style: {'max-width': '20px', 'max-height': '20px'}
            })),
            h('td', player.name),
            h('td', player.finished ? 'finished as ' + player.finished.rank : (player.ready || _.get(player.actions, 'length', 0)  ? 'ready' : '')),
        ])
    })

    rows.unshift(h('tr', [h('th', ''), h('th', 'name'), h('th', 'state')]))

    return h('div', [
        h('h4', 'Players: '),
        h('table', rows)
    ])
}

function renderScenarioPreview(name, scenario, actionHandler){
    var board = renderBoard(scenario)
    var robots = scenario.initialRobots.map((robot, index) => renderRobot(index, robot, false))
    robots.unshift(board)
    return h('article.media', h('div.media-content', [
        h('h4', name),
        h('div.board', robots),
        button.primary(actionHandler, {selectScenario: scenario}, 'Select this Scenario')
    ]))
}

function renderBoard(scenario) {
    return h('board', _.range(scenario.height).map(function (r) {
        return h('row', _.range(scenario.width).map(function (c) {
            return renderCell(scenario, r, c)
        }))
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

function renderRobot(index, robot, finished) {
    var x = robot.position.x * 50
    var y = robot.position.y * 50
    var rot = directionToRotation(robot.direction)

    return h('robot.robot' + (index % 6 + 1),
        {
            style: {
                transform: 'translate(' + x + 'px, ' + y + 'px) rotate(' + rot + 'deg)',
                opacity: finished ? '0' : '1',
            },
            props: {
                id: 'robot_' + (index % 6 + 1)
            }
        });
}


function renderPlayerRobots(players) {
    return h('div', players.map(function (player) {
        return renderRobot(player.index, player.robot, player.finished)
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


        options.unshift(h('option', (slot + 1) + ' unselected'))
        return h('select',
            {
                props: {
                    disabled: !player || player.finished
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
    }

    if(! player) {
        return h('div.control-panel', h('div.text', 'observer mode'))
    }else if(player.finished){
        return h('div.control-panel', h('div.text', 'target reached'))
    }else {
        return h('div.control-panel', _.range(constants.numberOfActionsPerCycle).map(actionSelect))
    }
}

function renderLog(logs) {
    return h('div', [
        h('h4', 'Log: '),
        h('div', logs && logs.length ? logs.map(log => h('div', log)) : [])
    ])
}

module.exports = render
