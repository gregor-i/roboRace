const h = require('snabbdom/h').default
const button = require('../common/button')
const {renderScenario} = require('../game/gameBoard/static')
const images = require('../common/images')

function render(state, actionHandler) {
  const hook = function(x, y){
    const elm = y ? y.elm : x.elm
    const tiles = elm.getElementsByClassName("tile")

    function clickListener(event) {
      const tileX = parseInt(event.target.dataset.x)
      const tileY = parseInt(event.target.dataset.y)

      const bb = event.target.getBoundingClientRect()
      const direction = dxdy2direction(event.x - bb.x - bb.width/2, event.y - bb.y - bb.height/2)
      clickEventHandler(state.clickAction, actionHandler)(tileX, tileY, direction)
    }

    for(let i=0; i< tiles.length; i++) {
      tiles[i].onclick = clickListener
    }
  }

  return h('div.game', [
    fab('.fab-right-1', images.iconClose, [actionHandler, {backToLobby: true}]),
    h('div.game-board', renderScenario(state.scenario, {hook: {postpatch: hook, insert: hook}})),
    renderEditorActionbar(actionHandler)
  ])
}

function fab(classes, image, onclick){
  return h('div.fab'+classes, {on: {click: onclick}},
    h('img', {props: {src: image.src}}))
}

function renderEditorActionbar(actionHandler) {
  return h('div.footer-group', [
    h('div.slots-panel', [
      button.builder(actionHandler, {setClickAction: 'ToggleWall'}, 'Wall'),
      button.builder(actionHandler, {setClickAction: 'TogglePit'}, 'Pit'),
      button.builder(actionHandler, {setClickAction: 'SetTarget'}, 'Target'),
      button.builder(actionHandler, {setClickAction: 'ToggleInitialRobot'}, 'Set Robot'),
      button.builder(actionHandler, {setClickAction: 'RotateRobot'}, 'Rotate Robot')
    ]),
    h('div.slots-panel', [
      button.builder(actionHandler, 'width--', 'W-'),
      button.builder(actionHandler, 'width++', 'W+'),
      button.builder(actionHandler, 'height--', 'H-'),
      button.builder(actionHandler, 'height++', 'H+'),
      button.builder(actionHandler, 'save', 'Save Scenario')
    ])
  ])
}

function dxdy2direction(dx, dy) {
  switch (Math.floor((Math.atan2(dy, dx) / Math.PI * 3 + 6) % 6)) {
    case 0: return {DownRight: {}}
    case 1: return {Down: {}}
    case 2: return {DownLeft: {}}
    case 3: return {UpLeft: {}}
    case 4: return {Up: {}}
    case 5: return {UpRight: {}}
  }
}

function clickEventHandler(clickAction, actionHandler) {
  if (clickAction === 'ToggleWall')
    return (x, y, direction) => actionHandler({toggleWall: {x, y, direction}})
  else if (clickAction === 'TogglePit')
    return (x, y) => actionHandler({togglePit: {x, y}})
  else if (clickAction === 'SetTarget')
    return (x, y) => actionHandler({setTarget: {x, y}})
  else if (clickAction === 'ToggleInitialRobot')
    return (x, y) => actionHandler({toggleInitialRobot: {x, y}})
  else if (clickAction === 'RotateRobot')
    return (x, y) => actionHandler({rotateRobot: {x, y}})
  else
    return (x, y, direction) => {}
}

module.exports = render
