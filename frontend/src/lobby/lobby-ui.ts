import {h} from 'snabbdom'
import {button, group} from '../common/button'
import {images} from '../common/images'
import * as _ from 'lodash'
import {LobbyState} from "../state";

export function render(state: LobbyState, actionHandler) {
  return h('div', [
    h('section.hero.is-primary', [
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
    renderGameList(state, actionHandler),
    renderScenarioList(state, actionHandler)
  ])

}

function card(content) {
  return h('div.card', {style: {'marginBottom': '16px'}},
    h('div.card-content', content))
}

function mediaObject(left, content) {
  return h('article.media', [
    left ? h('figure.media-left',
      h('p.image', {style: {width: '64px', height: '64px'}},
        left
      )
    ) : null,
    h('div.media-content', content)
  ])
}

function robotImage(index, filled, you) {
  return h('img', {
    class: {
      'robot-tile': true,
      'robot-tile-you': you
    },
    props: {
      src: filled ? images.player(index) : images.playerStart(index)
    }
  })
}


function renderPlayerSlots(game) {
  return game.scenario.initialRobots.map(robot =>
    robotImage(
      robot.index,
      game.robots.find(r => r.index === robot.index),
      _.get(game.you, 'index') === robot.index
    )
  )
}

function gameState(game) {
  if (game.cycle === 0)
    return 'Game waiting for players'
  else
    return 'Game running'
}

function renderGameList(state: LobbyState, actionHandler) {
  function renderGame(gameRow) {
    return card(mediaObject(null, [
      h('div', [h('strong', 'state: '), gameState(gameRow)]),
      h('div', renderPlayerSlots(gameRow)),
      h('button.button.is-light.is-primary', {on: {click: () => actionHandler({openGame: gameRow})}}, 'Enter')
    ]))
  }

  return h('section.section',
    h('div.container', [
      h('h4.title', 'Game List: '),
      ...state.games.map(renderGame)
    ]))
}

function renderScenarioList(state: LobbyState, actionHandler) {
  function renderScenario(scenarioRow) {
    return card(mediaObject(null, [
      h('div', [h('strong', 'Scenario description: '), scenarioRow.description]),
      h('div', scenarioRow.scenario.initialRobots.map(robot => robotImage(robot.index, false, false))),
      group(
        h('button.button.is-light.is-primary', {on: {click: () => actionHandler({openScenario: scenarioRow})}}, 'Enter'),
        h('button.button.is-light', {on: {click: () => actionHandler({editScenario: scenarioRow.id})}}, 'Edit'),
        h('button.button.is-light.disabled', 'Delete')
      )]
    ))
  }

  return h('section.section',
    h('div.container', [
      h('h4.title', 'Scenario List: '),
      ...state.scenarios.map(renderScenario)
    ]))
}