const h = require('snabbdom/h').default
const button = require('../common/button')
const modal = require('../common/modal')
const images = require('../common/images')
const {renderScenario} = require('../game/gameBoard/static')
const _ = require('lodash')

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
        renderGameList(state, state.games, actionHandler),
        renderScenarioList(state.player, state.scenarios, actionHandler),
        m
      ])

}

function renderGameList(state, games, actionHandler) {
  function renderPlayerSlots(game) {
    return game.scenario.initialRobots.map(robot =>
        h('img', {
          style: {
            background: (_.get(game.you, 'index') === robot.index ? 'radial-gradient(closest-side, #1d97e2, rgba(0,0,0,0)), ' : '')
                + 'url(' + images.tile + ')'
          },
          props: {
            src: game.robots.find(r => r.index === robot.index) ?
                images.player(robot.index) :
                images.playerStart(robot.index)
          }
        }))
  }

  function renderGame(game, index) {
    const content = h('article.media', [
      // h('figure.media-left',
      //     h('p.image', {style: {width: '64px', height: '64px'}},
      //         h('img', {props: {src: images.player(index)}})
      //     )
      // ),
      h('div.media-content',
          h('div.content', [
                h('p', [h('strong', 'state: '), 'game.state']),
                h('p', renderPlayerSlots(game)),
                button.group(
                    button.builder.primary()(actionHandler, {openGame: game.id}, 'Enter'),
                    button.builder.disabled(game.owner !== state.player)(actionHandler, {deleteGame: game.id}, 'Delete')
                )
              ]
          )
      )
    ])

    return h('div.card', {style: {'marginBottom': '16px'}},
        h('div.card-content', content))
  }

  return h('section.section',
      h('div.container', [
        h('h4.title', 'Game List: '),
        ...games.map(renderGame)
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
