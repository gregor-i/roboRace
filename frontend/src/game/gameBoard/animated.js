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

function initializeRobots(game) {
  return game.players.map(player => {
    const initial = game.scenario.initialRobots[player.index]
    return animateRotation(player.index, initial.direction, initial.direction, 0, 0)
      + animateTranslation(player.index, initial.position, initial.position, 0, 0)
  })
}

function animate(events){
  function t(i){
    return eventSequenceDuration(_.take(events, i))
  }

  const functions = {
    RobotTurns: (event, data, i) =>
      animateRotation(data.playerIndex, data.from, data.to, t(i), eventDuration(event))
    ,
    RobotMoves: (event, data, i) => data.transitions
        .map(transition => animateTranslation(transition.playerIndex, transition.from, transition.to, t(i), eventDuration(event)))
        .join("")
    ,
    RobotReset: (event, data, i) =>
      animateRotation(data.playerIndex, data.from.direction, data.to.direction, t(i), eventDuration(event))
        + animateTranslation(data.playerIndex, data.from.position, data.to.position, t(i), eventDuration(event))
  }

  return events.map((event, i) => {
    const f = functions[Object.keys(event)[0]]
    return f ? f(event, Object.values(event)[0], i) : ''
  }).join("")
}

function gameSvg(game) {
  return `
<svg xmlns="http://www.w3.org/2000/svg"
  width="${svg.width(game.scenario)}"
  height="${svg.height(game.scenario)}"
  duration="${eventSequenceDuration(game.events)}"
  viewBox="0 0 ${svg.width(game.scenario)} ${svg.height(game.scenario)}">
  <defs>${svg.defs}</defs>
  <g transform="scale(${svg.tile}) translate(0.5 0.5)">
    <g>${svg.tiles(game.scenario)}</g>
    <g>${svg.walls(game.scenario)}</g>
    <g>${svg.target(game.scenario)}</g>
    <g>${svg.startingPoints(game.scenario)}</g>
    <g>${svg.robots(game)}</g>
  </g>
  <g name="animation">
    ${initializeRobots(game)}
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
    console.log({oldDuration, newDuration})
    if(oldDuration !== newDuration){
      newSvg.dataset.duration = newDuration
      newSvg.setCurrentTime(Math.min(oldTime, oldDuration))
      console.log({oldTime, oldDuration, min: Math.min(oldTime, oldDuration)})
    }
  }
  return h('div', {props: {innerHTML: gameSvg(game)}, hook: {insert, prepatch, postpatch}})
}

function eventDuration(event){
  return event.RobotTurns || event.RobotMoves || event.RobotReset ? 0.5 : 0
}

function eventSequenceDuration(events){
  return _.sumBy(events, eventDuration)
}

module.exports = {renderGame}
