const _ = require('lodash')
const h = require('snabbdom/h').default
const constants = require('../common/constants')
const button = require('../common/button')
const modal = require('../common/modal')
const {renderGame, eventSequenceDuration} = require('./gameBoard/animated')
const images = require('../common/images')


function render(state, actionHandler) {
  if (state.game) {
    let m = null
    const closeAction = [actionHandler, {closeModal: true}]
    if (state.modal === 'log')
      m = modal(renderLog(state.game.events), closeAction)

    const playerIndex = _.get(state.game.you, "index")
    return h('div.game', [
      fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
      fab('.fab-left-1', images.iconReplayAnimation, event => {
        const svg = document.querySelector('.game-board svg')
        if (svg)
          svg.setCurrentTime(eventSequenceDuration(_.takeWhile(state.game.events, event => !event.StartCycleEvaluation || event.StartCycleEvaluation.cycle !== state.game.cycle - 1)))
      }),
      renderGame(state.game),
      renderActionButtons(state, actionHandler),
      m])
  } else {
    return h('div.game', [
      fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
      renderGame({scenario: state.scenario.scenario, robots: []}),
      renderActionButtons(state, actionHandler)
    ])
  }
}

function fab(classes, image, onclick){
  return h('div.fab'+classes, {on: {click: onclick}},
      h('img', {props: {src: image}}))
}

function robotImage(index, filled, you, onclick) {
  return h('img', {
    class: {
      'robot-tile': true,
      'robot-tile-you': you
    },
    props: {
      src: filled ? images.player(index) : images.playerStart(index)
    },
    on: {click: onclick}
  })
}

function renderActionButtons(state, actionHandler) {
  const focusedSlot = state.focusedSlot || 0
  const player = _.get(state, 'game.you')

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

  if (!state.game) {
    return h('div.text-panel', state.scenario.scenario.initialRobots
        .map(robot => robotImage(robot.index, false, false, () => actionHandler({createGame: robot.index}))))
  } else if (!player && _.get(state, 'game.cycle') === 0) {
    return h('div.text-panel', state.game.scenario.initialRobots
        .map(robot => robotImage(robot.index, false, false, () => actionHandler({joinGame: robot.index}))))
  } else if (!player) {
    return h('div.text-panel', 'observer mode')
  } else if (player.finished && player.finished.rageQuitted === false) {
    return h('div.text-panel', 'target reached')
  } else if (player.finished && player.finished.rageQuitted === true) {
    return h('div.text-panel', 'game quitted')
  } else {
    let instructionTypes = []
    let instr = _.clone(player.instructionOptions)
    while (instr.length !== 0) {
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
