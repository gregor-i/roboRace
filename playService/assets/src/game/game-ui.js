var _ = require('lodash')
var h = require('snabbdom/h').default
var constants = require('../common/constants')
var button = require('../common/button')
var modal = require('../common/modal')
var frame = require('../common/frame')
var images = require('../common/images')

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
            h('div.content', Object.keys(state.scenarios).map(key => renderScenarioPreview(key, state.scenarios[key], actionHandler))),
            undefined,
            m)
    }else if (state.game.GameStarting) {
        const game = state.game.GameStarting
        return frame(header('Game ' + state.gameId, [
                backToLobbyButton(actionHandler),
                joinGameButton(state, game, actionHandler),
                readyButton(state, game, actionHandler)
            ]),
            h('div.content', renderPlayerList(state)),
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
            renderCanvas(state, game.scenario, game.players),
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

function renderCanvas(state, scenario, robotsOrPlayers) {
    function drawCanvas(canvas){
        const ctx = canvas.getContext("2d")

        const rect = canvas.getBoundingClientRect()
        canvas.width = rect.width;
        canvas.height = rect.height;

        const tileWidth = rect.width/(scenario.width * 1.1 -0.1)
        const tileHeight = rect.height/(scenario.height * 1.1 -0.1)

        const tile = Math.min(tileHeight, tileWidth)
        const wall = tile / 10

        const offsetLeft = (canvas.width - (scenario.width * tile + (scenario.width - 1) * wall)) / 2
        const offsetTop = (canvas.height - (scenario.height * tile + (scenario.height - 1) * wall)) / 2

        function left(x){
            return offsetLeft + (tile + wall) * x
        }
        function top(y){
            return offsetTop + (tile + wall) * y
        }

        // scenario:
        {
            // tiles:
            ctx.fillStyle = 'lightgrey'
            for (let y = 0; y < scenario.height; y++)
                for (let x = 0; x < scenario.width; x++)
                    ctx.fillRect(left(x), top(y), tile, tile)

            // walls:
            ctx.fillStyle = 'black'
            scenario.walls.forEach(function (w) {
                if (w.direction.Right)
                    ctx.fillRect(left(w.position.x) + tile, top(w.position.y), wall, tile)
                else if (w.direction.Down)
                    ctx.fillRect(left(w.position.x), top(w.position.y) + tile, tile, wall)
            })

            // target:
            {
                ctx.fillStyle = 'green'
                ctx.fillRect(left(scenario.targetPosition.x), top(scenario.targetPosition.y), tile, tile)
            }

            // pits:
            ctx.fillStyle = 'white'
            scenario.pits.forEach(pit =>
                ctx.fillRect(left(pit.x), top(pit.y), tile, tile)
            )
        }


        // robots:
        robotsOrPlayers.forEach((player) => {
            ctx.save()
            const robot = player.robot
            ctx.translate(left(robot.position.x) + tile / 2, top(robot.position.y) + tile / 2)
            console.log(directionToRotation(robot.direction))
            ctx.rotate(directionToRotation(robot.direction))
            ctx.drawImage(images.player(player.index), -tile / 2, -tile / 2, tile, tile)
            ctx.restore()
        })
    }


    return h('canvas.game-view', {
            hook: {
                postpatch: (oldVnode, newVnode) => drawCanvas(newVnode.elm),
                insert: (node)  => {
                    window.onresize = () => drawCanvas(node.elm)
                    drawCanvas(node.elm)
                },
                destroy: () => window.onresize = undefined
            }
        }
    )
}

function renderScenarioPreview(name, scenario, actionHandler){
    return h('article.media', h('div.media-content', [
        h('h4', name),
        button.primary(actionHandler, {selectScenario: scenario}, 'Select this Scenario')
    ]))
}

function directionToRotation(direction) {
    if (direction.Up)
        return 0
    else if (direction.Right)
        return Math.PI/2
    else if (direction.Down)
        return Math.PI
    else if (direction.Left)
        return Math.PI/2+Math.PI
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
