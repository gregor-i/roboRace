const _ = require('lodash')
const h = require('snabbdom/h').default
const button = require('../common/button')
const frame = require('../common/frame')
const modal = require('../common/modal')
const gameBoard = require('../game/game-board')

function render(state, actionHandler) {
  let m = null
  const closeAction = [actionHandler, {closeModal: true}]
  if (state.modal && state.modal.type === 'previewScenario')
    m = modal(h('div.modal-maximized',
        gameBoard.renderCanvas(state.modal.scenario, state.modal.scenario.initialRobots.map(gameBoard.robotFromInitial), {}),
        ), closeAction)

  const backToLobby = backToLobbyButton(actionHandler)

  if(state.scenario) {
    return frame([h('h1', 'Scenario Editor: ' + state.scenarioId), button.group(backToLobby)],
        gameBoard.renderCanvas(state.scenario, state.scenario.initialRobots.map(gameBoard.robotFromInitial), clickEventHandler(state.clickAction, actionHandler)),
        renderEditorActionbar(actionHandler),
        m)
  } else {
    return frame([h('h1', 'Scenario Editor:'), button.group(backToLobby)],
        h('div.content',renderScenarioList(state.player, state.scenarios, actionHandler)),
        undefined,
        m)
  }
}

function clickEventHandler(clickAction, actionHandler){
  if(clickAction === 'TogglePit')
    return {onClickTile: (x, y) => actionHandler({togglePit: {x, y}})}
  else if(clickAction === 'SetTarget')
    return {onClickTile: (x, y) => actionHandler({setTarget: {x, y}})}
  else if(clickAction === 'ToggleInitialRobot')
    return {onClickTile: (x, y) => actionHandler({toggleInitialRobot: {x, y}})}
  else if(clickAction === 'RotateRobot')
    return {onClickTile: (x, y) => actionHandler({rotateRobot: {x, y}})}
  else
    return {}
}

function renderEditorActionbar(actionHandler){
  return [
    h('div.control-panel', [
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

function renderScenarioList(player, scenarios, actionHandler) {
  const header = h('tr', [h('th', 'Id'), h('th', 'owner'), h('th', 'actions')])

  const rows = scenarios.map(row =>
      h('tr', [
        h('td', row.id),
        h('td', row.owner),
        h('td', button.group(
            button.primary(actionHandler, {editScenario: row}, 'Edit this Scenario'),
            button.builder(actionHandler, {setModal: {type: 'previewScenario', scenario:row.scenario}}, 'Preview'),
            button.builder.disabled(row.owner !== player)(actionHandler, {deleteScenario: row.id}, 'Delete')
        ))
      ]))

  return h('div', [
    h('h4', 'Select a scenario: '),
    h('table', [header, ...rows])
  ])
}

function backToLobbyButton(actionHandler) {
  return button.link(actionHandler, {backToLobby: true}, 'Back to Lobby')
}

module.exports = render
