const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

const animationSpeed = 0.5

function animationDebugInfo(game){
return `<g class="animationDebugInfo">
    ${game.events.map((event, i) => {
      return `
    <text display="none" x="0" y="15" fill="black">
        ${Object.keys(event)[0]}, ${JSON.stringify(event)}
        <set attributeName="display" to="block" begin="${animationSpeed*i}s" dur="${animationSpeed}s" />
    </text>
      `}).join("")
    }
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

function animateRotation(playerIndex, from, to, t){
  return `<animateTransform attributeName="transform" type="rotate"
                            xlink:href="#robot-rotation-${playerIndex}"
                            from="${svg.directionToRotation(from)}"
                            to="${nearestRotationTarget(svg.directionToRotation(from), svg.directionToRotation(to))}"
                            begin="${t}s" dur="${animationSpeed}s" fill="freeze" />`
}

function animateTranslation(playerIndex, from, to, t) {
  return `<animateTransform attributeName="transform" type="translate"
                            xlink:href="#robot-translation-${playerIndex}"
                            from="${svg.left(from.x,from.y)} ${svg.top(from.x,from.y)}" 
                            to="${svg.left(to.x,to.y)} ${svg.top(to.x,to.y)}"
                            begin="${t}s" dur="${animationSpeed}s" fill="freeze" />`
}

function initializeRobots(game) {
  return game.players.map(player => {
    const initial = game.scenario.initialRobots[player.index]
    return animateRotation(player.index, initial.direction, initial.direction, 0)
      + animateTranslation(player.index, initial.position, initial.position, 0)
  })
}

function animate(events){
  function t(i){
    return i * animationSpeed
  }

  const functions = {
    RobotTurns: (event, i) =>
      animateRotation(event.playerIndex, event.from, event.to, t(i))
    ,
    RobotMoves: (event, i) => event.transitions
        .map(transition => animateTranslation(transition.playerIndex, transition.from, transition.to, t(i)))
        .join("")
    ,
    RobotReset: (event, i) =>
      animateRotation(event.playerIndex, event.from.direction, event.to.direction, t(i))
        + animateTranslation(event.playerIndex, event.from.position, event.to.position, t(i))
  }

  return events.map((event, i) => {
    const f = functions[Object.keys(event)[0]]
    return f ? f(Object.values(event)[0], i) : ''
  }).join("")
}

function gameSvg(game) {
  return `
<svg xmlns="http://www.w3.org/2000/svg"
  width="${svg.width(game.scenario)}"
  height="${svg.height(game.scenario)}"
  duration="${time(game.events)}"
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
  const duration = time(game.events)
  var timeCache
  function prepatch(oldVNode, newVNode){
    const oldSvg = oldVNode.elm.getElementsByTagName('svg')[0]
    const time = oldSvg.getCurrentTime()
    timeCache = Math.min(duration, time)
    console.log("set timecache to "+timeCache)
  }
  function postpatch(oldVNode, newVNode){
    const newSvg = newVNode.elm.getElementsByTagName('svg')[0]
    newSvg.setCurrentTime(timeCache)
  }
  return h('div', {props: {innerHTML: gameSvg(game)}, hook: {prepatch, postpatch}})
}

function time(events){
  return events.length * animationSpeed
}

module.exports = {renderGame, time}
