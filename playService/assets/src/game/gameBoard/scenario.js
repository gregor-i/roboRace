const _ = require('lodash')
const h = require('snabbdom/h').default

const tile = 50

const deltaLeft = 0.75
const deltaTop = Math.sqrt(3) / 2

function polarX(angle, dist) {
  return dist * Math.cos(angle / 180 * Math.PI)
}

function polarY(angle, dist) {
  return dist * Math.sin(angle / 180 * Math.PI)
}

function init(f){
  return f()
}

const hexCoordinates = _.range(0,360, 60).map(angle => `${polarX(angle, 0.5)} ${polarY(angle, 0.5)}`).join(" ")
const wallCoordinates = init(() => {
  const cornerX = polarX(240, 0.5)
  const cornerY = polarY(240, 0.5)
  const diffX = polarX(240, 0.07)
  const diffY = polarY(240, 0.07)
  return `${cornerX} ${cornerY}
    ${cornerX - diffX} ${cornerY - diffY}
    ${-cornerX + diffX} ${cornerY - diffY}
    ${-cornerX} ${cornerY}
    ${-cornerX + diffX} ${cornerY + diffY}
    ${cornerX - diffX} ${cornerY + diffY}`
  })


function left(x, y) { return 0.75 * x }
function top(x, y) { return  deltaTop * (y + (x % 2) / 2) }
function height(scenario) { return  (top(0, scenario.height) + 0.5) * tile }
function width(scenario) { return  (left(scenario.width, 0) + (1-deltaLeft)) * tile }

function robotColor(index) {
  switch((index % 6 + 6) % 6){
    case 0 : return  "blue"
    case 1 : return  "green"
    case 2 : return  "red"
    case 3 : return  "orange"
    case 4 : return  "cyan"
    case 5 : return  "magenta"
  }
}

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

const defs = [
  `<polygon id="hex" points="${hexCoordinates}"/>`,
  `<clipPath id="hexagon-cutout"><use href="#hex"/></clipPath>`,
  `<linearGradient id="stroke-gradient" x1="4.425%" x2="98.163%" y1="3.974%" y2="97.917%">
        <stop offset="0%" stop-color="#FFF"/>
        <stop offset="100%" stop-color="#AFAFAF"/>
    </linearGradient>`,
  `<use id="tile"
          href="#hex"
          clip-path="url(#hexagon-cutout)"
          fill="#EAEAEA"
          stroke="url(#stroke-gradient)" stroke-width="0.07"/>`,
  `<polygon id="wall" points="${wallCoordinates}" fill="grey" stroke="black" stroke-width="0.02" />`,
  `<path id="target" fill="black"
           d="M6,3A1,1 0 0,1 7,4V4.88C8.06,4.44 9.5,4 11,4C14,4 14,6 16,6C19,6 20,4 20,4V12C20,12 19,14 16,14C13,14 13,12 11,12C8,12 7,14 7,14V21H5V4A1,1 0 0,1 6,3M7,7.25V11.5C7,11.5 9,10 11,10C13,10 14,12 16,12C18,12 18,11 18,11V7.5C18,7.5 17,8 16,8C14,8 13,6 11,6C9,6 7,7.25 7,7.25Z"
           transform="scale(0.66) translate(-0.5 -0.5) scale(${1.0/24})"/>`,
  `<g id="robot" fill-rule="evenodd" transform="scale(0.6) translate(-0.5 -0.5) scale(${1.0/32}) translate(2 6)">
        <circle cx="14" cy="13" r="7" />
        <path d="M4 0v16H0V0zM28 0v16h-4V0z"/>
        <path d="M2 8l8 4v5l-8-3zM25 8l-8 4v5l8-3zM9 3h1v6H9zM18 3h1v6h-1z"/>
        <path fill="#FFF" d="M12.012 9.46a1 1 0 0 0-1.732 1c.277.48 2.009-.52 1.732-1zM17.72 10.46a1 1 0 0 0-1.732-1c-.277.48 1.455 1.48 1.732 1z"/>
        <path d="M7 15h14v5.333L14 23l-7-2.667z"/>
    </g>`
]

function translate(x, y){
  return `translate(${left(x, y)} ${top(x, y)})`
}

function useTile(x, y){
  return `<use href="#tile" transform="${translate(x, y)}"/>`
}

function useWall(x, y, rotation){
  return `<use href="#wall" transform="${translate(x, y)} rotate(${rotation})"/>`
}

function useTarget(x, y){
  return `<use href="#target" transform="${translate(x, y)}"/>`
}

function useRobot(x, y, color, direction){
  return `<use href="#robot" fill="${color}" transform="${translate(x, y)} rotate(${direction})"/>`
}

function svg(scenario, excludeInitialRobots) {
  const tiles = _.range(0, scenario.width).map(x => {
    return _.range(0, scenario.height)
      .filter(y => !scenario.pits.find(p => p.x === x && p.y === y))
      .map(y => useTile(x, y))
      .join("")
  }).join("")

  const walls = scenario.walls.map(w => useWall(w.position.x, w.position.y, directionToRotation(w.direction))).join("")

  const target = useTarget(scenario.targetPosition.x, scenario.targetPosition.y)

  const robots = excludeInitialRobots ? '' : scenario.initialRobots.map((robot, index) =>
    useRobot(robot.position.x, robot.position.y, robotColor(index), directionToRotation(robot.direction))
  )

  return `
<svg xmlns="http://www.w3.org/2000/svg"
  width="${width(scenario)}"
  height="${height(scenario)}"
  viewBox="0 0 ${width(scenario)} ${height(scenario)}">
  <defs>
    ${defs.join("")}
  </defs>
  <g transform="scale(${tile}) translate(0.5 0.5)">
      ${[tiles, walls, target, robots].join("")}
  </g>
</svg>`
}

function render(scenario) {
  return h('object', {props: {innerHTML: svg(scenario)}})
}


module.exports = {render, svg}