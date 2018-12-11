import {h} from 'snabbdom'
import * as _ from 'lodash'
import {width, height, directionToRotation, left, top, tiles, walls, target, traps, startingPoints, robots} from './svg'



function nearestRotationTarget(from, to) {
  const a = Math.abs(from - to)
  const b = Math.abs(from - to + 360)
  const c = Math.abs(from - to - 360)
  if(a < b && a < c)
    return to
  else if(b < c)
    return to - 360
  else
    return to + 360
}

function eventDuration(event){
  const durs = {
    RobotReset: 1,
    RobotTurns: 0.5,
    RobotMoves: 0.5,
    PlayerJoinedGame: 0.5,
    PlayerQuitted: 0.5,
    PlayerFinished: 0.5,
    TrapEffect: 0.5
  }
  return durs[Object.keys(event)[0]] || 0
}

export function eventSequenceDuration(events){
  return _.sumBy(events, eventDuration)
}

function animateRotation(playerIndex, from, to, begin, duration) {
  return h('animateTransform', {
    attrs: {
      attributeName: "transform",
      type: "rotate",
      'xlink:href': `#robot-rotation-${playerIndex}`,
      from: directionToRotation(from),
      to: nearestRotationTarget(directionToRotation(from), directionToRotation(to)),
      begin: `${begin}s`,
      dur: duration === 0 ? 'indefinite' : duration + 's',
      fill: "freeze"
    }
  })
}

function animateTranslation(playerIndex, from, to, begin, duration) {
  return h('animateTransform', {
    attrs: {
      attributeName: "transform",
      type: "translate",
      'xlink:href': `#robot-translation-${playerIndex}`,
      from: `${left(from.x, from.y)} ${top(from.x, from.y)}`,
      to: `${left(to.x, to.y)} ${top(to.x, to.y)}`,
      begin: begin + 's',
      dur: duration === 0 ? 'indefinite' : duration + 's',
      fill: "freeze"
    }
  })
}

function animateScale(playerIndex, from, to, begin, duration) {
  return h('animateTransform', {
    attrs: {
      attributeName: "transform",
      type: "scale",
      'xlink:href': `#robot-scale-${playerIndex}`,
      from: from,
      to: to,
      begin: begin + "s",
      dur: duration === 0 ? 'indefinite' : duration + "s",
      fill: "freeze"
    }
  })
}

function animateSpawn(playerIndex, robot, begin, duration){
  return [animateRotation(playerIndex, robot.direction, robot.direction, begin, 0),
     animateTranslation(playerIndex, robot.position, robot.position, begin, 0),
     animateScale(playerIndex, 0, 1, begin, duration)]
}

function animateDespawn(playerIndex, robot, begin, duration){
  return [animateRotation(playerIndex, robot.direction, robot.direction, begin, 0),
       animateTranslation(playerIndex, robot.position, robot.position, begin, 0),
       animateScale(playerIndex, 1, 0, begin, duration)]
}

function animate(events){
  function t(i){
    return eventSequenceDuration(_.take(events, i))
  }

  const functions = {
    RobotTurns: (data, i, duration) =>
        animateRotation(data.playerIndex, data.from, data.to, t(i), duration),
    RobotMoves: (data, i, duration) => data.transitions
        .map(transition => animateTranslation(transition.playerIndex, transition.from, transition.to, t(i), duration)),
    RobotReset: (data, i, duration) =>
        [animateDespawn(data.playerIndex, data.from, t(i), duration / 2),
          animateSpawn(data.playerIndex, data.to, t(i) + duration / 2, duration / 2)],
    PlayerJoinedGame: (data, i, duration) =>
        animateSpawn(data.playerIndex, data.robot, t(i), duration),
    PlayerQuitted: (data, i, duration) =>
        animateDespawn(data.playerIndex, data.robot, t(i), duration),
    PlayerFinished: (data, i, duration) =>
        animateDespawn(data.playerIndex, data.robot, t(i), duration)
  }

  return _.flatMap(events, (event, i) => {
    const f = functions[Object.keys(event)[0]]
    if(f){
      const duration = eventDuration(event)
        const r = f(Object.values(event)[0], i, duration)
      if(r.length)
        return _.flatten(r)
      else
        return r
    }else{
      return []
    }
  })
}

export function renderGame(game) {
  var oldDuration, oldTime
  function insert(vnode){
    const svg = vnode.elm.getElementsByTagName('svg')[0]
    svg.dataset.duration = eventSequenceDuration(game.events)
  }
  function prepatch(oldVNode, newVNode){
    const oldSvg = oldVNode.elm.getElementsByTagName('svg')[0]
    oldDuration = parseFloat(oldSvg.dataset.duration) || 0
    oldTime = oldSvg.getCurrentTime()
  }
  function postpatch(oldVNode, newVNode){
    const newSvg = newVNode.elm.getElementsByTagName('svg')[0]
    const newDuration = eventSequenceDuration(game.events)
    if (oldDuration !== newDuration) {
      newSvg.dataset.duration = newDuration
      newSvg.setCurrentTime(Math.min(oldTime, oldDuration))
    }
  }

  return h('div.game-board',
    {hook: {insert, prepatch, postpatch}},
    h('svg', {attrs: {xmlns: "http://www.w3.org/2000/svg", viewBox: `0 0 ${width(game.scenario)} ${height(game.scenario)}`}}, [
      h('g', tiles(game.scenario, undefined)),
      h('g', walls(game.scenario)),
      h('g', target(game.scenario)),
      h('g', traps(game.scenario)),
      h('g', startingPoints(game.scenario)),
      h('g', robots(game)),
      h('g', {attrs: {name: "animation"}}, animate(game.events))
    ]))
}