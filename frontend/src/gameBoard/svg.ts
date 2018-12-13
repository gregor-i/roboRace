import {h} from 'snabbdom'
import * as _ from 'lodash'

import {images} from '../common/images'
import {Scenario} from "../models";

const deltaLeft = 0.75
const deltaTop = Math.sqrt(3) / 2

export function left(x, y) { return deltaLeft * x }
export function top(x, y) { return  deltaTop * (y + ((x % 2 + 2) % 2) / 2) }
export function height(scenario) { return  (top(0, scenario.height) + 0.5) }
export function width(scenario) { return  (left(scenario.width, 0) + (1-deltaLeft)) }

export function directionToRotation(dir) {
  switch (Object.keys(dir)[0]) {
    case 'Up' : return 0
    case 'UpRight' : return 60
    case 'DownRight' : return 120
    case 'Down' : return 180
    case 'DownLeft' : return 240
    case 'UpLeft' : return 300
  }
}

export function translate(x, y){
  return `translate(${left(x, y)} ${top(x, y)})`
}

function imageAttrs(src, additionalAttrs?) {
  return _.merge({
    href: src,
    width: '1',
    height: '1',
    x: '0',
    y: '0',
  }, additionalAttrs)
}

export function tiles(scenario, tileClickListener) {
  return _.flatMap(_.range(0, scenario.width), x =>
      _.range(0, scenario.height)
          .map(y => h('image.tile', {
            attrs: imageAttrs(images.tile, {'data-x': x, 'data-y': y, transform: translate(x, y)}),
            style: {opacity: !scenario.pits.find(p => p.x === x && p.y === y) ? '1' : '0'},
            on: {click: tileClickListener}
          }))
  )
}

export function walls(scenario) {
  return scenario.walls.map(w => h('image', {
    attrs: imageAttrs(images.wall(w.direction), {
      transform: `${translate(w.position.x, w.position.y)}`
    })
  }))
}

export function target(scenario) {
  return h('image', {
    attrs: imageAttrs(images.target, {
      transform: translate(scenario.targetPosition.x, scenario.targetPosition.y)
    })
  })
}

export function traps(scenario) {
  return scenario.traps.map(function (trap) {
    if (trap.TurnRightTrap)
      return h('image', {
        attrs: imageAttrs(images.trapTurnRight, {
          transform: translate(trap.TurnRightTrap.position.x, trap.TurnRightTrap.position.y)
        })
      })
    else if (trap.TurnLeftTrap)
      return h('image', {
        attrs: imageAttrs(images.trapTurnLeft, {
          transform: translate(trap.TurnLeftTrap.position.x, trap.TurnLeftTrap.position.y)
        })
      })
    else if (trap.StunTrap)
      return h('use', {
        attrs: imageAttrs(images.trapStun, {
          transform: translate(trap.StunTrap.position.x, trap.StunTrap.position.y)
        })
      })
  })
}

export function startingPoints(scenario: Scenario) {
  return scenario.initialRobots.map((robot, index) =>
      h('image', {
        attrs: imageAttrs(images.playerStart(index), {
          transform: `${translate(robot.position.x, robot.position.y)} rotate(${directionToRotation(robot.direction)})`
        })
      })
  )
}

export function robots(game) {
  return game.robots.map(robot =>
      h('g', {attrs: {transform: 'translate(0.5 0.5)'}},
          h(`g#robot-translation-${robot.index}`, {attrs: {transform: translate(robot.position.x, robot.position.y)}},
              h(`g#robot-rotation-${robot.index}`, {attrs: {transform: `rotate(${directionToRotation(robot.direction)})`}},
                  h(`g#robot-scale-${robot.index}`, {attrs: {transform: 'scale(1)'}},
                      h('g', {attrs: {transform: 'translate(-0.5 -0.5)'}},
                          h('image', {attrs: imageAttrs(images.player(robot.index))})
                      )
                  ))
          )
      )
  )
}
