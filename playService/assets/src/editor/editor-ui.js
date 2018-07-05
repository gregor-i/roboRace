const h = require('snabbdom/h').default
const button = require('../common/button')
const frame = require('../common/frame')
const gameBoard = require('../game/game-board')

function render(state, actionHandler) {
  return frame([h('h1', 'Scenario Editor: ' + state.scenarioId), button.group(backToLobbyButton(actionHandler))],
    gameBoard.renderCanvas(state.scenario, state.scenario.initialRobots.map(gameBoard.robotFromInitial), clickEventHandler(state.clickAction, actionHandler)),
    renderEditorActionbar(actionHandler),
    null)
}

function clickEventHandler(clickAction, actionHandler) {
  if (clickAction === 'ToggleWall')
    return {onClickTile: (x, y, direction) => actionHandler({toggleWall: {x, y, direction}})}
  else if (clickAction === 'TogglePit')
    return {onClickTile: (x, y) => actionHandler({togglePit: {x, y}})}
  else if (clickAction === 'SetTarget')
    return {onClickTile: (x, y) => actionHandler({setTarget: {x, y}})}
  else if (clickAction === 'ToggleInitialRobot')
    return {onClickTile: (x, y) => actionHandler({toggleInitialRobot: {x, y}})}
  else if (clickAction === 'RotateRobot')
    return {onClickTile: (x, y) => actionHandler({rotateRobot: {x, y}})}
  else
    return {}
}

function renderEditorActionbar(actionHandler) {
  return [
    h('div.control-panel', [
      button.builder(actionHandler, {setClickAction: 'ToggleWall'}, 'Wall'),
      button.builder(actionHandler, {setClickAction: 'TogglePit'}, 'Pit'),
      button.builder(actionHandler, {setClickAction: 'SetTarget'}, 'Target'),
      button.builder(actionHandler, {setClickAction: 'ToggleInitialRobot'}, 'Set Robot'),
      button.builder(actionHandler, {setClickAction: 'RotateRobot'}, 'Rotate Robot')
    ]),
    h('div.control-panel', [
      button.builder(actionHandler, 'width--', 'W-'),
      button.builder(actionHandler, 'width++', 'W+'),
      button.builder(actionHandler, 'height--', 'H-'),
      button.builder(actionHandler, 'height++', 'H+'),
      button.builder(actionHandler, 'save', 'Save Scenario')
    ])
  ]
}

function backToLobbyButton(actionHandler) {
  return button.link(actionHandler, {backToLobby: true}, 'Back to Lobby')
}

module.exports = render
