const _ = require('lodash')
const h = require('snabbdom/h').default
const button = require('../common/button')
const frame = require('../common/frame')
const modal = require('../common/modal')
const renderCanvas = require('../game/game-board').renderCanvas

function render(state, actionHandler) {
  var m = null
  if (state.modal === 'log')
    m = modal(renderLog(state.logs), [actionHandler, {setModal: 'none'}])
  else if (state.modal === 'playerList')
    m = modal(renderPlayerList(state), [actionHandler, {setModal: 'none'}])


  let width = h('input.input',
    {props: {value: state.scenario.width}, on: {change: function(){ actionHandler({setWidth: width.elm.value})}}}
  )
  let height = h('input.input',
    {props: {value: state.scenario.height}, on: {change: function(){ actionHandler({setHeight: height.elm.value})}}}
  )

  return frame([h('h1', 'Scenario Editor:'), button.group(backToLobbyButton(actionHandler))],
    [
      renderCanvas(state.scenario, state.scenario.initialRobots.map(initialRobotForCanvas)),
      h('controls',
        {style: {position: 'absolute', top: '0', right: '0'}},
        [
          doubleInput('Size:',
            width,
            height
          ),
          doubleInput('Target:',
            h('input.input',
              {props: {value: state.scenario.targetPosition.x}}
            ),
            h('input.input',
              {props: {value: state.scenario.targetPosition.y}}
            )
          ),
        ]
      )
    ],
    undefined,
    m)
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

function initialRobotForCanvas(robot, index) {
  return {index, x: robot.position.x, y: robot.position.y, alpha: 1}
}

function backToLobbyButton(actionHandler) {
  return button.link(actionHandler, {backToLobby: true}, 'Back to Lobby')
}

module.exports = render