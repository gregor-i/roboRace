const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

function animationDebugInfo(game) {
  return `<g class="animationDebugInfo">
    ${game.events.map((event, i) => {
      const duration = eventDuration(event)
      if (duration !== 0)
        return `<text display="none" x="0" y="15" fill="black">
                  ${Object.keys(event)[0]}, ${JSON.stringify(event)}
                  <set attributeName="display" to="block" begin="${eventSequenceDuration(_.take(game.events, i))}s" dur="${duration}s" />
                </text>`
      else
        return ""
      }).join("")}
  </g>`
}

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

function animateRotation(playerIndex, from, to, begin, duration){
  return `<animateTransform attributeName="transform" type="rotate"
                            xlink:href="#robot-rotation-${playerIndex}"
                            from="${svg.directionToRotation(from)}"
                            to="${nearestRotationTarget(svg.directionToRotation(from), svg.directionToRotation(to))}"
                            begin="${begin}s" dur="${duration === 0 ? 'indefinite' : duration}s" fill="freeze" />`
}

function animateTranslation(playerIndex, from, to, begin, duration) {
  return `<animateTransform attributeName="transform" type="translate"
                            xlink:href="#robot-translation-${playerIndex}"
                            from="${svg.left(from.x,from.y)} ${svg.top(from.x,from.y)}" 
                            to="${svg.left(to.x,to.y)} ${svg.top(to.x,to.y)}"
                            begin="${begin}s" dur="${duration === 0 ? 'indefinite' : duration}s" fill="freeze" />`
}

function animateScale(playerIndex, from, to, begin, duration){
  return `<animateTransform attributeName="transform" type="scale"
                            xlink:href="#robot-scale-${playerIndex}"
                            from="${from}" 
                            to="${to}"
                            begin="${begin}s" dur="${duration === 0 ? 'indefinite' : duration}s" fill="freeze" />`
}

function animateSpawn(playerIndex, robot, begin, duration){
  return animateRotation(playerIndex, robot.direction, robot.direction, begin, 0)
    + animateTranslation(playerIndex, robot.position, robot.position, begin, 0)
    + animateScale(playerIndex, 0, 1, begin, duration)
}

function animateDespawn(playerIndex, robot, begin, duration){
  return animateRotation(playerIndex, robot.direction, robot.direction, begin, 0)
      + animateTranslation(playerIndex, robot.position, robot.position, begin, 0)
      + animateScale(playerIndex, 1, 0, begin, duration)
}

function animate(events){
  function t(i){
    return eventSequenceDuration(_.take(events, i))
  }

  const functions = {
    RobotTurns: (data, i, duration) =>
      animateRotation(data.playerIndex, data.from, data.to, t(i), duration)
    ,
    RobotMoves: (data, i, duration) => data.transitions
        .map(transition => animateTranslation(transition.playerIndex, transition.from, transition.to, t(i), duration))
        .join("")
    ,
    RobotReset: (data, i, duration) =>
      animateDespawn(data.playerIndex, data.from, t(i), duration / 2)
      + animateSpawn(data.playerIndex, data.to, t(i) + duration / 2, duration / 2)
    ,
    PlayerJoinedGame: (data, i, duration) =>
        animateSpawn(data.playerIndex, data.robot, t(i), duration),
    PlayerFinished: (data, i, duration) =>
        animateDespawn(data.playerIndex, data.robot, t(i), duration)
  }

  return events.map((event, i) => {
    const f = functions[Object.keys(event)[0]]
    if(f){
      const duration = eventDuration(event)
      return f(Object.values(event)[0], i, duration)
    }else{
      return ''
    }
  }).join("")
}

function gameSvg(game) {
  const height = svg.height(game.scenario)
  const width = svg.width(game.scenario)
  return `
<svg xmlns="http://www.w3.org/2000/svg"
  style="min-width:${width}px; min-height:${height}px; max-width:${3*width}px; max-height=${3*height}px; display: block; margin: auto;"
  viewBox="0 0 ${width} ${height}">
  <defs>${svg.defs}</defs>
  <g transform="scale(${svg.tile}) translate(0.5 0.5)">
    <g>${svg.tiles(game.scenario)}</g>
    <g>${svg.walls(game.scenario)}</g>
    <g>${svg.target(game.scenario)}</g>
    <g>${svg.startingPoints(game.scenario)}</g>
    <g>${svg.robots(game)}</g>
  </g>
  <g name="animation">
    ${animate(game.events)}
  </g>
  <g name="debug-info">
    ${animationDebugInfo(game)}
  </g>
</svg>`
}

function renderGame(game) {
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
    if(oldDuration !== newDuration){
      newSvg.dataset.duration = newDuration
      newSvg.setCurrentTime(Math.min(oldTime, oldDuration))
    }
  }
  return h('div.game-board', {props: {innerHTML: gameSvg(game)}, hook: {insert, prepatch, postpatch}})
}

function eventDuration(event){
  const durs = {
    RobotReset: 1,
    RobotTurns: 0.5,
    RobotMoves: 0.5,
    PlayerJoinedGame: 0.5,
    PlayerFinished: 0.5
  }
  return durs[Object.keys(event)[0]] || 0
}

function eventSequenceDuration(events){
  return _.sumBy(events, eventDuration)
}

module.exports = {renderGame}
