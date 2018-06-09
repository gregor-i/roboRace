var h = require('snabbdom/h').default
var button = require('../common/button')
var modal = require('../common/modal')
var frame = require('../common/frame')

function render(state, actionHandler) {
    return frame([h('h1', 'Game Lobby:'), button.group(
        button.builder.primary()(actionHandler, {createGame: true}, 'New Game'),
        button.builder(actionHandler, {redirectTo: '/editor'}, 'Scenario Editor'),
        button.builder(actionHandler, {reloadGameList: true}, 'Refresh'),
        button.builder(actionHandler, {resetUserName: true}, 'Logout')
        )],
        h('div.content', renderGameTable(state, state.games, actionHandler)),
        undefined,
        renderLoginModal(state.player, actionHandler)
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
        button.builder.primary()(actionHandler, {enterGame: row.id}, 'Enter'),
        button.builder.disable(row.owner !== state.player)(actionHandler, {deleteGame: row.id}, 'Delete')
    ))
  ]))

  return h('table', [header, ...rows])
}

function renderLoginModal(player, actionHandler) {
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

  function submit() {
    var player = input.elm.value
    if (player)
      actionHandler({definePlayerName: player})
  }

  if (!player) {
    return modal([
      h('h3', 'Login'),
      input,
      h('a.button.is-primary', {on: {click: submit}}, 'Enter')
    ])
  } else {
    return undefined
  }
}

module.exports = render
