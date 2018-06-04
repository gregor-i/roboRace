const _ = require('lodash')
const h = require('snabbdom/h').default
const constants = require('../common/constants')
const button = require('../common/button')
const modal = require('../common/modal')
const frame = require('../common/frame')
const gameBoard = require('./game-board')
const images = require('../common/images')


function render(state, actionHandler) {
  var m = null
  if (state.modal === 'log')
    m = modal(renderLog(state.logs), [actionHandler, {setModal: 'none'}])
  else if (state.modal === 'playerList')
    m = modal(renderPlayerList(state), [actionHandler, {setModal: 'none'}])

  if (state.game.InitialGame) {
    const game = state.game.InitialGame
    return frame(header('Initial Game', [
          backToLobbyButton(actionHandler),
        ]),
        h('div.content', renderScenarioList(state.scenarios, actionHandler)),
        undefined,
        m)
  } else if (state.game.GameStarting) {
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
        gameBoard.renderCanvas(state, game.scenario, game.players.map(gameBoard.robotFromPlayer)),
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

function header(title, buttons) {
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

  var rows = players.map(function (player) {
    return h('tr', [
      h('td', h('img', {
        props: {src: images.player(player.index).src},
        style: {'max-width': '20px', 'max-height': '20px'}
      })),
      h('td', player.name),
      h('td', player.finished ? 'finished as ' + player.finished.rank : (player.ready || _.get(player.actions, 'length', 0) ? 'ready' : '')),
    ])
  })

  const header = h('tr', [h('th', ''), h('th', 'name'), h('th', 'state')])

  return h('div', [
    h('h4', 'Players: '),
    h('table', [header, ...rows])
  ])
}

function renderScenarioList(scenarios, actionHandler) {
  const header = h('tr', [h('th', 'Id'), h('th', 'owner'), h('th', 'actions')])

  const rows = scenarios.map(row =>
      h('tr', [
        h('td', row.id),
        h('td', row.owner),
        h('td', button.group(
            button.primary(actionHandler, {selectScenario: row.scenario}, 'Select this Scenario'),
            button.builder.disable(true)(actionHandler, {previewScenario: row.scenario}, 'Preview')
        ))
      ]))

  return h('table', [header, ...rows])
}

function renderActionButtons(state, game, actionHandler) {
  const player = game.players.find((player) => player.name === state.player)

  function actionSlot(index) {
    const action = state.slots[index]
    const image = action !== undefined ? images.action(Object.keys(player.possibleActions[action])[0]) : undefined
    return h('div.slot', {
          class: {
            'slot-selected': action !== undefined
          },
          on: {
            dragover: ev => ev.preventDefault(),
            drop: ev => {
              const actionIndex = parseInt(ev.dataTransfer.getData("actionIndex"))
              actionHandler({defineAction: {slot: index, value: actionIndex, cycle: game.cycle}})
            }
          }
        },
        image !== undefined ? h('img', {props: {src: image.src}}) : undefined)
  }

  function action(action, index) {
    const isUsed = state.slots.find(s => s === index) !== undefined
    const image = images.action(Object.keys(action)[0])
    return h('div.action', {
      class: {'action-used': isUsed},
      props: {draggable: !isUsed},
      on: {dragstart: (ev) => ev.dataTransfer.setData("actionIndex", index)}
    }, h('img', {props: {src: image.src}}))
  }

  if (!player) {
    return h('div.control-panel', h('div.text', 'observer mode'))
  } else if (player.finished) {
    return h('div.control-panel', h('div.text', 'target reached'))
  } else {
    return [
      h('div.control-panel', player.possibleActions.map(action)),
      h('div.control-panel', _.range(constants.numberOfActionsPerCycle).map(actionSlot))
    ]
  }
}

function renderLog(logs) {
  return h('div', [
    h('h4', 'Log: '),
    h('div', logs && logs.length ? logs.map(log => h('div', log)) : [])
  ])
}

module.exports = render
