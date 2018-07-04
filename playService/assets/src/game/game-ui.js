const _ = require('lodash')
const h = require('snabbdom/h').default
const constants = require('../common/constants')
const button = require('../common/button')
const modal = require('../common/modal')
const frame = require('../common/frame')
const gameBoard = require('./game-board')
const images = require('../common/images')


function render(state, actionHandler) {
  let m = null
  const closeAction = [actionHandler, {closeModal: true}]
  if (state.modal === 'log')
    m = modal(renderLog(state.logs), closeAction)
  else if (state.modal === 'playerList')
    m = modal(renderPlayerList(state), closeAction)
  else if (state.modal && state.modal.type === 'previewScenario')
    m = modal(h('div.modal-maximized',
        gameBoard.renderCanvas(state.modal.scenario, state.modal.scenario.initialRobots.map(gameBoard.robotFromInitial), {}),
        ),
        closeAction)

  const backToLobby = button.link(actionHandler, {leaveGame: true}, 'Back to Lobby')

  if (state.game.InitialGame) {
    return frame(header('Initial Game', [
          backToLobby,
        ]),
        h('div.content', renderScenarioList(state.scenarios, actionHandler)),
        undefined,
        m)
  } else if (state.game.GameStarting) {
    const game = state.game.GameStarting
    return frame(header('Game ' + state.gameId, [
          backToLobby,
          button.builder.primary().disabled(!!game.players.find(player => player.name === state.player))(actionHandler, {joinGame: state.gameId}, 'Join Game'),
          button.builder.disabled(_.get(game.players.find(player => player.name === state.player), 'ready')).primary()(actionHandler, {readyForGame: state.gameId}, 'Ready')
        ]),
        h('div.content', renderPlayerList(state)),
        undefined,
        m)
  } else if (state.game.GameRunning || state.game.GameFinished) {
    const game = state.game.GameRunning || state.game.GameFinished
    return h('div', [
      fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
      fab('.fab-left-1', images.iconReplayAnimation, [actionHandler, {replayAnimations: state.animations}]),
      fab('.fab-left-2', images.iconGamerlist, [actionHandler, {setModal: 'playerList'}]),
      h('div.game-board', gameBoard.renderCanvas(game.scenario, game.players.map(gameBoard.robotFromPlayer), {
        animationStart: state.animationStart,
        frames: state.animations
      })),
      renderActionButtons(state, game, actionHandler),
      m])
  } else {
    return frame(header('GameState \'undefined\' is currently not supported.', [
          backToLobby,
        ]),
        undefined,
        undefined,
        m)
  }
}

function fab(classes, image, onclick){
  return h('div.fab'+classes, {on: {click: onclick}},
      h('img', {props: {src: image.src}}))
}

function header(title, buttons) {
  return button.group(buttons)
}

function renderPlayerList(state) {
  let players = []
  if (state.game.GameStarting)
    players = state.game.GameStarting.players
  else if (state.game.GameRunning)
    players = state.game.GameRunning.players
  else if (state.game.GameFinished)
    players = state.game.GameFinished.players

  let rows = players.map(function (player) {
    return h('tr', [
      h('td', h('img', {
        props: {src: images.player(player.index).src},
        style: {'max-width': '20px', 'max-height': '20px'}
      })),
      h('td', player.name),
      h('td', player.finished ? 'finished as ' + player.finished.rank : (player.ready || _.get(player.instructions, 'length', 0) ? 'ready' : '')),
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
            button.builder(actionHandler, {setModal: {type: 'previewScenario', scenario:row.scenario}}, 'Preview')
        ))
      ]))

  return h('div', [
    h('h4', 'Select a scenario: '),
    h('table', [header, ...rows])
  ])
}

function renderActionButtons(state, game, actionHandler) {
  const player = game.players.find((player) => player.name === state.player)
  const focusedSlot = state.focusedSlot || 0

  let instructionTypes = []
  let instr = _.clone(player.instructionOptions)
  while(instr.length !== 0) {
    let head = instr[0]
    let type = Object.keys(head)[0]
    instructionTypes.push(type)
    instr = _.dropWhile(instr, i => i[type])
  }

  function instructionCard(type) {
    function unusedAndThisType(opt, index) {
      return opt[type] && !_.some(state.slots, slot => slot === index)
    }

    const image = images.action(type)
    const count = player.instructionOptions.filter(unusedAndThisType).length
    const unusedIndex = _.findIndex(player.instructionOptions, unusedAndThisType)
    return h('div.action',
        {on: {click: count !== 0 ? () => actionHandler({defineInstruction: {slot: focusedSlot, value: unusedIndex}}) : undefined}},
        [h('img', {props: {src: image.src}}), h('div.badge', count)])
  }

  function instructionSlot(index) {
    const instruction = state.slots[index]
    const focused = focusedSlot === index
    const props = {class: {"slot-focused": focused},
    on:{click: () => actionHandler({focusSlot: index})}}
    if (instruction !== undefined) {
      const image = images.action(Object.keys(player.instructionOptions[instruction])[0])
      return h('div.slot.slot-filled', props,
          h('img', {props: {src: image.src}}))
    } else {
      return h('div.slot', props, index+1)
    }
  }

  if (!player) {
    return h('div.control-panel', h('div.text', 'observer mode'))
  } else if (player.finished) {
    return h('div.control-panel', h('div.text', 'target reached'))
  } else {
    return h('div.footer-group', [
      h('div.slots-panel', _.range(constants.numberOfInstructionsPerCycle).map(instructionSlot)),
      h('div.cards-panel', instructionTypes.map(instructionCard))
    ])
  }
}

function renderLog(logs) {
  return h('div', [
    h('h4', 'Log: '),
    h('div', logs && logs.length ? logs.map(log => h('div', log)) : [])
  ])
}

module.exports = render
