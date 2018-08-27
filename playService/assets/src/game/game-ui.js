const _ = require('lodash')
const h = require('snabbdom/h').default
const constants = require('../common/constants')
const button = require('../common/button')
const modal = require('../common/modal')
const gameBoard = require('./game-board')
const images = require('../common/images')


function render(state, actionHandler) {
  let m = null
  const closeAction = [actionHandler, {closeModal: true}]
  if (state.modal === 'log')
    m = modal(renderLog(state.game.events), closeAction)
  else if (state.modal === 'playerList')
    m = modal(renderPlayerList(state), closeAction)
  else if (state.modal && state.modal.type === 'previewScenario')
    m = modal(h('div.modal-maximized',
        gameBoard.renderCanvas(state.modal.scenario, state.modal.scenario.initialRobots.map(gameBoard.robotFromInitial), {}),
        ),
        closeAction)

  const game = state.game
  const playerIndex = _.get(game.players.find(p => p.name === state.player), "index")
  return h('div.game', [
    fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
    fab('.fab-left-1', images.iconReplayAnimation, [actionHandler, {replayAnimations: state.animations}]),
    fab('.fab-left-2', playerIndex !== undefined ? images.player(playerIndex) : images.iconGamerlist, [actionHandler, {setModal: 'playerList'}]),
    h('div.game-board', gameBoard.renderCanvas(game.scenario, game.players.map(gameBoard.robotFromPlayer), {
      animationStart: state.animationStart,
      frames: state.animations
    })),
    renderActionButtons(state, game, actionHandler),
    m])
}

function fab(classes, image, onclick){
  return h('div.fab'+classes, {on: {click: onclick}},
      h('img', {props: {src: image.src}}))
}

function renderPlayerList(state) {
  let rows = state.game.players.map(function (player) {
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

function renderActionButtons(state, game, actionHandler) {
  const focusedSlot = state.focusedSlot || 0
  const player = game.players.find((player) => player.name === state.player)

  function instructionCard(type) {
    function unusedAndThisType(opt, index) {
      return opt[type] && !_.some(player.instructionSlots, slot => slot === index)
    }

    const image = images.action(type)
    const count = player.instructionOptions.filter(unusedAndThisType).length
    const unusedIndex = _.findIndex(player.instructionOptions, unusedAndThisType)
    return h('div.action',
        {on: {click: count !== 0 ? () => actionHandler({setInstruction: true, slot: focusedSlot, instruction: unusedIndex}) : undefined}},
        [h('img', {props: {src: image.src}}), h('div.badge', count)])
  }

  function instructionSlot(index) {
    const instruction = player.instructionSlots[index]
    const focused = focusedSlot === index
    const props = {
      class: {"slot-focused": focused},
      on: {
        dblclick: () => actionHandler({resetSlot: true, slot: index}),
        click: () => actionHandler({focusSlot: index})
      }
    }
    if (instruction !== undefined && instruction !== null) {
      const image = images.action(Object.keys(player.instructionOptions[instruction])[0])
      return h('div.slot.slot-filled', props,
          h('img', {props: {src: image.src}}))
    } else {
      return h('div.slot', props, index+1)
    }
  }

  if (!player && game.cycle === 0) {
    return h('div.footer-group', [
      h('div.slots-panel', 'observer mode'),
      h('div.cards-panel', button.builder.primary()(actionHandler, {joinGame: state.gameId}, 'Join Game'))
    ])
  } else if (!player) {
    return h('div.status-panel', h('div.text', 'observer mode'))
  } else if (player.finished) {
    return h('div.status-panel', h('div.text', 'target reached'))
  } else {
    let instructionTypes = []
    let instr = _.clone(player.instructionOptions)
    while(instr.length !== 0) {
      let head = instr[0]
      let type = Object.keys(head)[0]
      instructionTypes.push(type)
      instr = _.dropWhile(instr, i => i[type])
    }

    return h('div.footer-group', [
      h('div.slots-panel', _.range(constants.numberOfInstructionsPerCycle).map(instructionSlot)),
      h('div.cards-panel', instructionTypes.map(instructionCard))
    ])
  }
}

function renderLog(events) {
  return h('div', [
    h('h4', 'Log: '),
    h('div', events.map(log => h('div', log)))
  ])
}

module.exports = render
