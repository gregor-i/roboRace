const h = require('snabbdom/h').default
const button = require('../common/button')
const modal = require('../common/modal')
const frame = require('../common/frame')
const {renderScenario} = require('../game/gameBoard/scenario')

function render(state, actionHandler) {
  let m = null
  if (!state.player) {
    m = renderLoginModal(state.player, actionHandler)
  } else if (state.previewScenario) {
    m = modal(renderScenario(state.previewScenario), [actionHandler, {closeModal: true}])
  }


  return frame([h('h1', 'Robo Race - Game Lobby:'), button.group(
    button.builder(actionHandler, {resetUserName: true}, 'Logout')
    )],
    h('div.content', [
      renderGameTable(state, state.games, actionHandler),
      renderScenarioList(state.player, state.scenarios, actionHandler)
    ]),
    undefined,
    m
  )
}

function renderGameTable(state, games, actionHandler) {
  const header = h('tr', [
    h('th', 'Id'),
    h('th', 'Owner'),
    h('th', 'State'),
    h('th', 'Actions'),
  ])

  const rows = games.map(row => h('tr', [
    h('td', row.id),
    h('td', row.owner),
    h('td', row.state),
    h('td', button.group(
      button.builder.primary()(actionHandler, {redirectTo: '/game/' + row.id}, 'Enter'),
      button.builder.disabled(row.owner !== state.player)(actionHandler, {deleteGame: row.id}, 'Delete')
    ))
  ]))

  return h('div', [
    h('h4', 'Game List: '),
    h('table', [header, ...rows])
  ])
}

function renderScenarioList(player, scenarios, actionHandler) {
  const header = h('tr', [h('th', 'Id'), h('th', 'owner'), h('th', 'actions')])

  const rows = scenarios.map(row =>
    h('tr', [
      h('td', row.id),
      h('td', row.owner),
      h('td', button.group(
        button.primary(actionHandler, {createGame: row.scenario}, 'Start Game'),
        button.builder(actionHandler, {editScenario: row.id}, 'Edit'),
        button.builder(actionHandler, {previewScenario: row.scenario}, 'Preview'),
        button.builder.disabled(row.owner !== player)(actionHandler, {deleteScenario: true, id: row.id}, 'Delete')
      ))
    ]))

  return h('div', [
    h('h4', 'Scenario List: '),
    h('table', [header, ...rows])
  ])
}

function renderLoginModal(player, actionHandler) {
  function submit() {
    if (input.elm.value)
      actionHandler({definePlayerName: input.elm.value})
  }

  let input = h('input.input.is-primary', {
      props: {
        placeholder: 'Name',
        id: 'player-name-input'
      },
      on: {
        keydown: function (event) {
          if (event.key === 'Enter')
            submit()
        }
      }
    }
  )

  return modal([
    h('h3', 'Login'),
    input,
    h('a.button.is-primary', {on: {click: submit}}, 'Enter')
  ])
}

module.exports = render
