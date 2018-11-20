const _ = require('lodash')
const h = require('snabbdom/h').default

const images = require('../../common/images')

const deltaLeft = 0.75
const deltaTop = Math.sqrt(3) / 2

function left(x, y) { return deltaLeft * x }
function top(x, y) { return  deltaTop * (y + ((x % 2 + 2) % 2) / 2) }
function height(scenario) { return  (top(0, scenario.height) + 0.5) }
function width(scenario) { return  (left(scenario.width, 0) + (1-deltaLeft)) }

function directionToRotation(dir) {
  switch (Object.keys(dir)[0]) {
    case 'Up' : return 0
    case 'UpRight' : return 60
    case 'DownRight' : return 120
    case 'Down' : return 180
    case 'DownLeft' : return 240
    case 'UpLeft' : return 300
  }
}

function translate(x, y){
  return `translate(${left(x, y)} ${top(x, y)})`
}

function imageAttrs(src, additionalAttrs) {
  return _.merge({
    href: src,
    width: '1',
    height: '1',
    x: '0',
    y: '0',
  }, additionalAttrs)
}

function tiles(scenario, tileClickListener) {
  return _.flatMap(_.range(0, scenario.width), x =>
      _.range(0, scenario.height)
          .map(y => h('image.tile', {
            attrs: imageAttrs(images.tile.src, {'data-x': x, 'data-y': y, transform: translate(x, y)}),
            style: {opacity: !scenario.pits.find(p => p.x === x && p.y === y) ? '1' : '0'},
            on: {click: tileClickListener}
          }))
  )
}

function walls(scenario) {
  return scenario.walls.map(w => h('image', {
    attrs: imageAttrs(images.wall(w.direction).src, {
      transform: `${translate(w.position.x, w.position.y)}`
    })
  }))
}

function target(scenario) {
  return h('image', {
    attrs: imageAttrs(images.target.src, {
      transform: translate(scenario.targetPosition.x, scenario.targetPosition.y)
    })
  })
}

function traps(scenario) {
  return scenario.traps.map(function (trap) {
    if (trap.TurnRightTrap)
      return h('image', {
        attrs: imageAttrs(images.trapTurnRight.src, {
          transform: translate(trap.TurnRightTrap.position.x, trap.TurnRightTrap.position.y)
        })
      })
    else if (trap.TurnLeftTrap)
      return h('image', {
        attrs: imageAttrs(images.trapTurnLeft.src, {
          transform: translate(trap.TurnLeftTrap.position.x, trap.TurnLeftTrap.position.y)
        })
      })
    else if (trap.StunTrap)
      return h('use', {
        attrs: imageAttrs(images.trapStun.src, {
          transform: translate(trap.StunTrap.position.x, trap.StunTrap.position.y)
        })
      })
  })
}

function startingPoints(scenario) {
  return scenario.initialRobots.map((robot, index) =>
      h('image', {
        attrs: imageAttrs(images.playerStart(index).src, {
          transform: `${translate(robot.position.x, robot.position.y)} rotate(${directionToRotation(robot.direction)})`
        })
      })
  )
}

function robots(game) {
  return game.players.map(player =>
      h('g', {attrs: {transform: 'translate(0.5 0.5)'}},
          h(`g#robot-translation-${player.index}`, {attrs: {transform: translate(player.robot.position.x, player.robot.position.y)}},
              h(`g#robot-rotation-${player.index}`, {attrs: {transform: `rotate(${directionToRotation(player.robot.direction)})`}},
                  h(`g#robot-scale-${player.index}`, {attrs: {transform: 'scale(1)'}},
                      h('g', {attrs: {transform: 'translate(-0.5 -0.5)'}},
                          h('image', {attrs: imageAttrs(images.player(player.index).src)})
                      )
                  ))
          )
      )
  )
}

module.exports = {
  directionToRotation, translate, left, top,
  width, height,
  tiles, walls, target, traps, startingPoints, robots,
}
