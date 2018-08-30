const _ = require('lodash')

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

const defTile = `
<polygon id="hex" points="${hexCoordinates}"/>
<clipPath id="hexagon-cutout"><use href="#hex"/></clipPath>
<linearGradient id="stroke-gradient" x1="4.425%" x2="98.163%" y1="3.974%" y2="97.917%">
  <stop offset="0%" stop-color="#FFF"/>
  <stop offset="100%" stop-color="#AFAFAF"/>
</linearGradient>
<use id="tile" href="#hex" clip-path="url(#hexagon-cutout)"
     fill="#EAEAEA" stroke="url(#stroke-gradient)" stroke-width="0.07"/>`

const defWall = `<polygon id="wall" points="${wallCoordinates}" fill="grey" stroke="black" stroke-width="0.02" />`
const defTarget = `<path id="target" fill="black"
           d="M6,3A1,1 0 0,1 7,4V4.88C8.06,4.44 9.5,4 11,4C14,4 14,6 16,6C19,6 20,4 20,4V12C20,12 19,14 16,14C13,14 13,12 11,12C8,12 7,14 7,14V21H5V4A1,1 0 0,1 6,3M7,7.25V11.5C7,11.5 9,10 11,10C13,10 14,12 16,12C18,12 18,11 18,11V7.5C18,7.5 17,8 16,8C14,8 13,6 11,6C9,6 7,7.25 7,7.25Z"
           transform="scale(0.66) translate(-0.5 -0.5) scale(${1.0/24})"/>`
const defRobot = `<g id="robot" fill-rule="evenodd" transform="scale(0.6) translate(-0.5 -0.5) scale(${1.0/32}) translate(2 6)">
        <circle cx="14" cy="13" r="7" />
        <path d="M4 0v16H0V0zM28 0v16h-4V0z"/>
        <path d="M2 8l8 4v5l-8-3zM25 8l-8 4v5l8-3zM9 3h1v6H9zM18 3h1v6h-1z"/>
        <path fill="#FFF" d="M12.012 9.46a1 1 0 0 0-1.732 1c.277.48 2.009-.52 1.732-1zM17.72 10.46a1 1 0 0 0-1.732-1c-.277.48 1.455 1.48 1.732 1z"/>
        <path d="M7 15h14v5.333L14 23l-7-2.667z"/>
    </g>`
const defRobotStartingPoint = `<g id="robot-starting-point" transform="scale(0.6) translate(-0.5 -0.5) scale(${1.0/32}) translate(2 6)">
    <path stroke-width="0.5" fill="none"
          d="M 12.50147,27.654979 9.0257664,26.328327 9.0117514,24.094098 8.9977365,21.859869 7.5121533,21.302303 C 6.6950826,20.995642 6.0171101,20.744568 6.0055478,20.744361 c -0.011562,-2.06e-4 -0.021022,0.27712 -0.021022,0.616282 V 21.9773 H 3.9944046 2.0042837 V 13.988787 6.0002735 H 3.9943194 5.984355 l 0.00709,4.5058015 0.00709,4.505802 1.6817923,0.846434 c 0.9249857,0.46554 1.6890912,0.847096 1.6980123,0.847904 0.00892,8.07e-4 0.063756,-0.121512 0.121855,-0.271822 C 9.7787,15.713879 10.250474,14.956779 10.825248,14.307961 l 0.176624,-0.199377 V 11.554027 8.9994696 h 0.490523 0.490523 V 11.12974 c 0,1.171648 0.0062,2.13027 0.01373,2.13027 0.0076,0 0.105305,-0.05871 0.217231,-0.130476 0.902804,-0.578831 1.96681,-0.959062 3.006491,-1.074394 0.384759,-0.04268 1.279885,-0.03478 1.659334,0.01464 0.99648,0.129797 2.010753,0.492947 2.83946,1.016642 l 0.266284,0.168276 0.0072,-2.127615 0.0072,-2.1276154 h 0.490348 0.490347 v 2.5501684 2.550168 l 0.199137,0.217781 c 0.426147,0.466048 0.801235,1.02007 1.094599,1.616772 0.09163,0.186374 0.175433,0.341314 0.186231,0.34431 0.0108,0.003 0.814278,-0.394327 1.785513,-0.882941 l 1.765882,-0.888389 0.0071,-4.253533 0.0071,-4.2535325 h 1.976016 1.976016 V 13.988787 21.9773 h -1.976106 -1.976106 v -0.798852 c 0,-0.620188 -0.0078,-0.798937 -0.03504,-0.799237 -0.01927,-2.24e-4 -0.700328,0.248822 -1.51346,0.55341 l -1.478422,0.553794 -0.0072,2.419806 -0.0072,2.419806 -3.489719,1.32936 c -1.919345,0.731148 -3.496026,1.328659 -3.503734,1.327802 -0.0077,-8.58e-4 -1.578081,-0.598551 -3.489719,-1.32821 z M 13.006008,16.49756 c 0.308782,-0.122677 0.571791,-0.290587 0.789404,-0.503971 0.316865,-0.310709 0.333621,-0.466498 0.07842,-0.729083 -0.407207,-0.418986 -1.069334,-0.405893 -1.466805,0.029 -0.08525,0.09328 -0.163442,0.215907 -0.200276,0.314096 -0.122396,0.32627 -0.02965,0.851252 0.168588,0.954317 0.108176,0.05624 0.402789,0.02617 0.630672,-0.06436 z m 6.626415,0.05733 c 0.132425,-0.06848 0.211922,-0.289484 0.212439,-0.590587 3.96e-4,-0.229578 -0.008,-0.270055 -0.09152,-0.439643 -0.17818,-0.361934 -0.498473,-0.56084 -0.903107,-0.56084 -0.262102,0 -0.477117,0.07627 -0.654033,0.231997 -0.162878,0.143371 -0.242923,0.273916 -0.242923,0.396183 0,0.454135 1.26272,1.178231 1.679144,0.96289 z"
          transform="translate(-2,-6)"/>
    </g>`

const defs = [defTile, defWall, defTarget, defRobot, defRobotStartingPoint].join("")

function translate(x, y){
  return `translate(${left(x, y)} ${top(x, y)})`
}

function useTile(x, y){
  return `<use href="#tile" class="tile" data-x="${x}" data-y="${y}" transform="${translate(x, y)}"/>`
}

function useWall(x, y, rotation){
  return `<use href="#wall" transform="${translate(x, y)} rotate(${rotation})"/>`
}

function useTarget(x, y){
  return `<use href="#target" transform="${translate(x, y)}"/>`
}

function useRobotStartingPoint(x, y, color, direction){
  return `<use href="#robot-starting-point" stroke="${color}" transform="${translate(x, y)} rotate(${direction})"/>`
}

function useRobot(x, y, color, direction){
  return `<use href="#robot" fill="${color}" transform="${translate(x, y)} rotate(${direction})"/>`
}

function tiles(scenario) {
  return _.range(0, scenario.width).map(x => {
    return _.range(0, scenario.height)
        .filter(y => !scenario.pits.find(p => p.x === x && p.y === y))
        .map(y => useTile(x, y))
        .join("")
  }).join("")
}

function walls(scenario){
  return  scenario.walls.map(w => useWall(w.position.x, w.position.y, directionToRotation(w.direction))).join("")
}

function target(scenario){
  return useTarget(scenario.targetPosition.x, scenario.targetPosition.y)
}

function startingPoints(scenario){
  return scenario.initialRobots.map((robot, index) =>
    useRobotStartingPoint(robot.position.x, robot.position.y, robotColor(index), directionToRotation(robot.direction))
  )
}

function robots(game){
  return game.players.map(player => {
    const robot = player.robot
    return useRobot(robot.position.x, robot.position.y, robotColor(player.index), directionToRotation(robot.direction))
  }).join("")
}

module.exports = {defs,
  tile, width, height,
  tiles, walls, target, startingPoints, robots}
