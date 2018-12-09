const _ = require('lodash')
const h = require('snabbdom/h').default
const constants = require('../common/constants')
const button = require('../common/button')
const modal = require('../common/modal')
const {renderGame, eventSequenceDuration} = require('./gameBoard/animated')
const images = require('../common/images')


function render(state, actionHandler) {
  let m = null
  const closeAction = [actionHandler, {closeModal: true}]
  if (state.modal === 'log')
    m = modal(renderLog(state.game.events), closeAction)
  else if (state.modal === 'playerList')
    m = modal(renderPlayerList(state), closeAction)

  const game = state.game
  const playerIndex = _.get(game.you, "index")
  return h('div.game', [
    fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
    fab('.fab-left-1', images.iconReplayAnimation, event => {
      const svg = document.querySelector('.game-board svg')
      if(svg)
        svg.setCurrentTime(eventSequenceDuration(_.takeWhile(game.events, event => !event.StartCycleEvaluation || event.StartCycleEvaluation.cycle !== game.cycle - 1)))
    }),
    fab('.fab-left-2', playerIndex !== undefined ? images.player(playerIndex) : images.iconGamerlist, [actionHandler, {setModal: 'playerList'}]),
    renderGame(game),
    renderActionButtons(state, game, actionHandler),
    m])
}

function fab(classes, image, onclick){
  return h('div.fab'+classes, {on: {click: onclick}},
      h('img', {props: {src: image}}))
}

function renderPlayerList(state) {
  let rows = state.game.players.map(function (player) {
    return h('tr', [
      h('td', h('img', {
        props: {src: images.player(player.index)},
        style: {'max-width': '20px', 'max-height': '20px'}
      })),
      h('td', player.name),
      h('td', player.finished ? 'finished as ' + player.finished.rank : (player.instructionSlots.filter(s => s !== null).length === constants.numberOfInstructionsPerCycle ? 'ready' : '')),
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
  const player = game.you

  function instructionCard(type) {
    function unusedAndThisType(opt, index) {
      return opt[type] && !_.some(player.instructionSlots, slot => slot === index)
    }

    const image = images.action(type)
    const count = player.instructionOptions.filter(unusedAndThisType).length
    const unusedIndex = _.findIndex(player.instructionOptions, unusedAndThisType)
    return h('div.action',
        {on: {click: count !== 0 ? () => actionHandler({setInstruction: true, slot: focusedSlot, instruction: unusedIndex}) : undefined}},
        [h('img', {props: {src: image}}), h('div.badge', count)])
  }

  function instructionSlot(index) {
    const instruction = player.instructionSlots[index]
    const focused = focusedSlot === index
    const props = {
      class: {focused},
      on: {
        dblclick: () => actionHandler({resetSlot: true, slot: index}),
        click: () => actionHandler({focusSlot: index})
      }
    }
    if (instruction !== undefined && instruction !== null) {
      const image = images.action(Object.keys(player.instructionOptions[instruction])[0])
      return h('span.slot.filled', props,
          h('img', {props: {src: image}}))
    } else {
      return h('span.slot', props, index+1)
    }
  }

  if (!player && game.cycle === 0) {
    return h('div.footer-group', [
      h('div.text-panel', 'observer mode'),
      h('div.text-panel', button.builder.primary()(actionHandler, {joinGame: state.gameId}, 'Join Game'))
    ])
  } else if (!player) {
    return h('div.text-panel', 'observer mode')
  } else if (player.finished) {
    return h('div.text-panel', 'target reached')
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
