import * as _ from 'lodash'
import {images} from '../common/images'
import {eventSequenceDuration, renderGame} from '../gameBoard/animated'
import {h} from 'snabbdom'
import {numberOfInstructionsPerCycle} from '../common/constants'
import {GameState} from "../state";

export function render(state: GameState, actionHandler) {
  return h('div.game', [
    fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
    fab('.fab-left-1', images.iconReplayAnimation, event => {
      const svg = document.querySelector('.game-board svg')
      if (svg)
        (<HTMLMediaElement>svg).currentTime = eventSequenceDuration(
          _.takeWhile(state.game.events, (event: any) => !event.StartCycleEvaluation || event.StartCycleEvaluation.cycle !== state.game.cycle - 1)
        )
    }),
    renderGame(state.game),
    renderActionButtons(state, actionHandler)
  ])
}

function fab(classes, image, onclick) {
  return h('div.fab' + classes, {on: {click: onclick}},
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

function renderActionButtons(state: GameState, actionHandler) {
  const focusedSlot = state.focusedSlot || 0
  const player: any = _.get(state, 'game.you')

  function instructionCard(type) {
    function unusedAndThisType(opt, index) {
      return opt[type] && !_.some(player.instructionSlots, slot => slot === index)
    }

    const image = images.action(type)
    const count = player.instructionOptions.filter(unusedAndThisType).length
    const unusedIndex = _.findIndex(player.instructionOptions, unusedAndThisType)
    return h('div.action',
      {
        on: {
          click: count !== 0 ? () => actionHandler({
            setInstruction: true,
            slot: focusedSlot,
            instruction: unusedIndex
          }) : undefined
        }
      },
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
      return h('span.slot', props, index + 1)
    }
  }

  if (!player && state.game.cycle === 0) {
    return h('div.text-panel', state.game.scenario.initialRobots
      .map(robot => robotImage(robot.index, false, false, () => actionHandler({joinGame: robot.index}))))
  } else if (!player) {
    return h('div.text-panel', 'observer mode')
  } else if (player.finished && player.finished.rageQuitted === false) {
    return h('div.text-panel', 'target reached as ' + player.finished.rank)
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
      h('div.slots-panel', _.range(numberOfInstructionsPerCycle).map(instructionSlot)),
      h('div.cards-panel', instructionTypes.map(instructionCard))
    ])
  }
}
