import {images} from '../common/images'
import {renderGame} from '../gameBoard/animated'
import {h} from 'snabbdom'
import {PreviewState} from "../state";

export function render(state: PreviewState, actionHandler) {
  return h('div.game', [
    fab('.fab-right-1', images.iconClose, [actionHandler, {leaveGame: true}]),
    renderGame({scenario: state.scenarioRow.scenario, robots: []}),
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

function renderActionButtons(state: PreviewState, actionHandler) {
  return h('div.text-panel', state.scenarioRow.scenario.initialRobots
    .map(robot => robotImage(robot.index, false, false, () => actionHandler({createGame: robot.index}))))
}
