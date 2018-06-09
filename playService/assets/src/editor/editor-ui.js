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
        gameBoard.renderCanvas(state.modal.scenario, state.modal.scenario.initialRobots.map(gameBoard.robotFromInitial)),
        ),
        closeAction)

  const backToLobby = backToLobbyButton(actionHandler)

  if(state.scenario) {
    let width = h('input.input',
        {props: {value: state.scenario.width}, on: {change: function(){ actionHandler({setWidth: width.elm.value})}}}
    )
    let height = h('input.input',
        {props: {value: state.scenario.height}, on: {change: function(){ actionHandler({setHeight: height.elm.value})}}}
    )
    return frame([h('h1', 'Scenario Editor: ' + state.scenarioId), button.group(backToLobby)],
        gameBoard.renderCanvas(state.scenario, state.scenario.initialRobots.map(gameBoard.robotFromInitial)),
        h('div.control-panel', [
          button.builder(actionHandler, 'width--', 'Widht -'),
          button.builder(actionHandler, 'width++', 'Widht +'),
          button.builder(actionHandler, 'height--', 'Height -'),
          button.builder(actionHandler, 'height++', 'Height +'),
          button.builder(actionHandler, 'save', 'Save Scenario')
        ]),
        m)
  } else {
    return frame([h('h1', 'Scenario Editor:'), button.group(backToLobby)],
        h('div.content',renderScenarioList(state.player, state.scenarios, actionHandler)),
        undefined,
        m)
  }
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

function doubleInput(label, input1, input2) {
  return h('div.field.is-horizontal',
    [
      h('div.field-label.is-normal',
        h('label.label', label)
      ),
      h('div.field-body', [
        h('div.field', h('p.control', input1)),
        h('div.field', h('p.control', input2)),
      ])
    ]
  )
}

function label(text) {
  return h('label.label', text)
}

function backToLobbyButton(actionHandler) {
  return button.link(actionHandler, {backToLobby: true}, 'Back to Lobby')
}

module.exports = render
