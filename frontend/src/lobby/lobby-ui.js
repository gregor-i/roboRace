const h = require('snabbdom/h').default
const button = require('../common/button')
const modal = require('../common/modal')
const {renderScenario} = require('../game/gameBoard/static')

function render(state, actionHandler) {
  let m = null
  if (state.previewScenario) {
    m = modal(renderScenario(state.previewScenario), [actionHandler, {closeModal: true}])
  }

  return h('div',
      [h('section.hero.is-primary', [
            h('div.hero-head', h('nav.navbar', h('div.container', h('div.navbar-end', [
                  h('a.navbar-item', {props: {href: 'https://github.com/gregor-i/roboRace'}}, 'Sources @ Github')
                ]
            )))),
            h('div.hero-body',
                h('div.container', [
                  h('h1.title.is-2', 'Robo Race'),
                  h('h2.subtitle.is-3', 'Game Lobby')
                ])
            )
          ]
      ),
        renderGameTable(state, state.games, actionHandler),
        renderScenarioList(state.player, state.scenarios, actionHandler),
        m
      ])

}

function renderGameTable(state, games, actionHandler) {
  const header = h('tr', [
    h('th', 'id'),
    h('th', 'owner'),
    h('th', 'state'),
    h('th', 'actions'),
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

  return h('section.section',
      h('div.container', [
        h('h4.title', 'Game List: '),
        h('table.table', [header, ...rows])
      ]))
}

function renderScenarioList(player, scenarios, actionHandler) {
  const header = h('tr', [h('th', 'player slots'), h('th', 'description'), h('th', 'actions')])

  const rows = scenarios.map(row =>
      h('tr', [
        h('td', row.scenario.initialRobots.length.toString()),
        h('td', row.description),
        h('td', button.group(
            button.primary(actionHandler, {createGame: row.scenario}, 'Start Game'),
            button.builder(actionHandler, {editScenario: row.id}, 'Edit'),
            button.builder(actionHandler, {previewScenario: row.scenario}, 'Preview'),
            button.builder.disabled(row.owner !== player)(actionHandler, {deleteScenario: true, id: row.id}, 'Delete')
        ))
      ]))

  return h('section.section',
      h('div.container', [
        h('h4.title', 'Scenario List: '),
        h('table.table', [header, ...rows])
      ]))
}

module.exports = render
