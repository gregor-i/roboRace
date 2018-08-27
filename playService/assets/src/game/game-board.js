const _ = require('lodash')
const h = require('snabbdom/h').default
const images = require('../common/images')
const constants = require('../common/constants')
const shapes = require('./shapes')

const k = Math.sqrt(3) / 2

function Robot(index, x, y, rotation, alpha) {
  return {index, x, y, rotation, alpha}
}

function robotFromPlayer(player) {
  return Robot(player.index, player.robot.position.x, player.robot.position.y, directionToRotation(player.robot.direction), player.finished ? 0 : 1)
}

function robotFromInitial(initial, index){
  return Robot(index, initial.position.x, initial.position.y, directionToRotation(initial.direction), 1)
}

function interpolate(d1, d2, t) {
  return (1 - t) * d1 + t * d2
}

function interpolateAngle(a0, a1, t) {
  // https://gist.github.com/shaunlebron/8832585
  const da = (a1 - a0) % (Math.PI * 2)
  return a0 + (2 * da % (Math.PI * 2) - da) * t
}

function drawCanvas(canvas, scenario, robots) {
  const ctx = canvas.getContext("2d")

  const rect = canvas.getBoundingClientRect()
  const width = rect.width
  const height = rect.height
  canvas.width = width
  canvas.height = height

  const wallFactor = 0.1

  const kWidth = scenario.width * 0.75  + (scenario.width - 1) * wallFactor * k + 0.25
  const kHeight = scenario.height * k + (scenario.height - 1) * wallFactor * k + 0.5 + wallFactor * 2

  const tile = Math.min(height / kHeight, width / kWidth)
  const offsetTop = (canvas.height - tile * kHeight) / 2
  const offsetLeft = (canvas.width - tile * kWidth) / 2

  const deltaLeft = 0.75 * tile + tile * wallFactor*k
  const deltaTop = tile * k + tile * wallFactor

  function left(x, y) {
    return offsetLeft + deltaLeft * x + tile /2
  }

  function top(x, y) {
    function saw(x) {
      const m = Math.abs(x) % 2
      if (m > 1) return 2 - m
      else return m
    }

    return offsetTop + deltaTop * (y + saw(x) / 2) + tile /2
  }

  const s = shapes(tile, wallFactor)

  function centerOn(x, y, callback) {
    // the same as:
    // centerOnInterpolated(x, y, x, y, 0, callback)
    ctx.save()
    ctx.translate(left(x, y), top(x, y))
    callback()
    ctx.restore()
  }

  function centerOnInterpolated(x1, y1, x2, y2, t, callback){
    ctx.save()
    ctx.translate(interpolate(left(x1, y1), left(x2, y2), t),
                  interpolate(top(x1, y1), top(x2, y2), t))
    callback()
    ctx.restore()
  }

  // tiles:
  for (let y = 0; y < scenario.height; y++)
    for (let x = 0; x < scenario.width; x++)
      centerOn(x, y, () => {
        if (scenario.pits.find(p => p.x === x && p.y === y))
          return

       ctx.fillStyle = 'rgb(240, 248, 255)'
       ctx.fill(s.hex)
       ctx.stroke(s.hex)
      })

  // target:
  centerOn(scenario.targetPosition.x, scenario.targetPosition.y, () => {
    ctx.drawImage(images.target, -tile / 4, -tile / 2 / 2, tile / 2, tile / 2)
  })

  // walls:
  scenario.walls.forEach(w =>
    centerOn(w.position.x, w.position.y, () => {
      ctx.fillStyle = 'DimGray'
      if (w.direction.Down) {
        ctx.fill(s.wallDown)
        ctx.stroke(s.wallDown)
      } else if (w.direction.DownRight) {
        ctx.fill(s.wallDownRight)
        ctx.stroke(s.wallDownRight)
      } else if (w.direction.UpRight) {
        ctx.fill(s.wallUpRight)
        ctx.stroke(s.wallUpRight)
      } else {
        console.error("unknown wall direction")
      }
    })
  )

    // robots:
  if (_.isArray(robots)) {
    robots.forEach(robot =>
      centerOn(robot.x, robot.y, () => {
        ctx.globalAlpha = robot.alpha
        ctx.rotate(robot.rotation)
        ctx.shadowBlur = 20
        ctx.shadowColor = "#383838"
        ctx.shadowOffsetX = 10
        ctx.shadowOffsetY = 10
        console.log(ctx)
        ctx.drawImage(images.player(robot.index), -tile / 4, -tile / 4, tile / 2, tile / 2)
      })
    )
  } else if (_.has(robots, 'currentFrame') && _.has(robots, 'nextFrame') && _.has(robots, 'frameProgress')) {
    const {currentFrame, nextFrame, frameProgress} = robots
    for (let i = 0; i < currentFrame.length; i++) {
      const current = currentFrame[i]
      const next = nextFrame[i]
      centerOnInterpolated(current.x, current.y, next.x, next.y, frameProgress, () => {
        ctx.globalAlpha = interpolate(current.alpha, next.alpha, frameProgress)
        ctx.rotate(interpolateAngle(current.rotation, next.rotation, frameProgress))
        ctx.shadowBlur = 20
        ctx.shadowColor = "#383838"
        ctx.shadowOffsetX = 10
        ctx.shadowOffsetY = 10
        ctx.drawImage(images.player(current.index), -tile / 4, -tile / 4, tile / 2, tile / 2)
      })
    }
  }
}

function drawAnimatedCanvas(canvas, startTime, scenario, frames, newStateRobots) {
  const now = new Date()
  const passedMillis = now - startTime
  const frameIndex = Math.floor(passedMillis / constants.animationFrameDuration)
  const frameProgress = (passedMillis - constants.animationFrameDuration * frameIndex) / constants.animationFrameDuration

  if (!frames || !frames[frameIndex] || !frames[frameIndex + 1]) {
    drawCanvas(canvas, scenario, newStateRobots)
  } else {
    const currentFrame = frames[frameIndex]
    const nextFrame = frames[frameIndex + 1]
    drawCanvas(canvas, scenario, {currentFrame, nextFrame, frameProgress})
    requestAnimationFrame(() => drawAnimatedCanvas(canvas, startTime, scenario, frames, newStateRobots))
  }
}

function onClickCanvas(scenario, options) {
  if(options.onClickTile)
    return (event) => {
      const canvas = event.target
      const rect = canvas.getBoundingClientRect()
      const width = rect.width
      const height = rect.height

      const wallFactor = 0.1

      const kWidth = scenario.width * 0.75  + (scenario.width - 1) * wallFactor * k + 0.25
      const kHeight = scenario.height * k + (scenario.height - 1) * wallFactor * k + 0.5 + wallFactor * 2

      const tile = Math.min(height / kHeight, width / kWidth)
      const offsetTop = (height - tile * kHeight) / 2
      const offsetLeft = (width - tile * kWidth) / 2

      const deltaLeft = 0.75 * tile + tile * wallFactor*k
      const deltaTop = tile * k + tile * wallFactor

      function left(x, y) {
        return offsetLeft + deltaLeft * x + tile /2
      }

      function top(x, y) {
        function saw(x) {
          const m = Math.abs(x) % 2
          if (m > 1) return 2 - m
          else return m
        }

        return offsetTop + deltaTop * (y + saw(x) / 2) + tile /2
      }

      const eventX = event.offsetX
      const eventY = event.offsetY

      let bestX = 0
      let bestY = 0

      function dist(x, y){
        return (eventX - left(x,y)) * (eventX - left(x,y)) + (eventY - top(x,y)) * (eventY - top(x,y))
      }

      for (let y = 0; y < scenario.height; y++)
        for (let x = 0; x < scenario.width; x++)
          if(dist(bestX, bestY) > dist(x, y)){
            bestX = x
            bestY = y
          }

      const angle = Math.atan2(top(bestX, bestY) - eventY, left(bestX, bestY) - eventX)
      // 0 = left
      // Math.PI = Right
      const directionHelper = Math.floor((angle/ Math.PI * 3 + 6) % 6)
      let direction
      switch(directionHelper){
        case 0:
          direction = {UpLeft: {}}
          break;
        case 1:
          direction = {Up: {}}
          break;
        case 2:
          direction = {UpRight: {}}
          break;
        case 3:
          direction = {DownRight: {}}
          break;
        case 4:
          direction = {Down: {}}
          break;
        case 5:
          direction = {DownLeft: {}}
          break;
      }

      if(Math.sqrt(dist(bestX, bestY)) < tile/2)
        options.onClickTile(bestX, bestY, direction)
    }
  else
    return null
}


function renderCanvas(scenario, robots, options) {
  return h('canvas.game-view', {
      on : {click: onClickCanvas(scenario, options)},
      hook: {
        postpatch: (oldVnode, newVnode) => {
          window.onresize = () => drawCanvas(newVnode.elm, scenario, robots)
          drawAnimatedCanvas(newVnode.elm, options.animationStart, scenario, options.frames, robots)
        },
        insert: node => {
          window.onresize = () => drawCanvas(node.elm, scenario, robots)
          drawCanvas(node.elm, scenario, robots)
        },
        destroy: () => window.onresize = undefined
      }
    }
  )
}

function directionToRotation(direction) {
  if (direction.Up)
    return 0
  else if (direction.UpRight)
    return Math.PI / 3
  else if (direction.DownRight)
    return Math.PI * 2 / 3
  else if (direction.Down)
    return Math.PI
  else if (direction.DownLeft)
    return Math.PI * 4 / 3
  else if (direction.UpLeft)
    return Math.PI * 5 / 3
  else
    throw new Error("unknown direction")
}

function framesFromEvents(oldGameState, events) {
  function indexByName(name) {
    return oldGameState.players.find((player) => player.name === name).index
  }

  let state = oldGameState.players.map(robotFromPlayer)
  let frames = []
  frames.push(_.cloneDeep(state))

  for (let j = 0; j < events.length; j++) {
    if (events[j].RobotMoves) {
      events[j].RobotMoves.transitions.forEach(t => {
        let i = indexByName(t.playerName)
        state[i].x = t.to.x
        state[i].y = t.to.y
      })
      frames.push(_.cloneDeep(state))
    } else if (events[j].RobotTurns) {
      let i = indexByName(events[j].RobotTurns.playerName)
      state[i].rotation = directionToRotation(events[j].RobotTurns.to)
      frames.push(_.cloneDeep(state))
    } else if (events[j].RobotReset) {
      let i = indexByName(events[j].RobotReset.playerName)
      state[i].alpha = 0.0
      frames.push(_.cloneDeep(state))
      state[i].x = events[j].RobotReset.to.position.x
      state[i].y = events[j].RobotReset.to.position.y
      state[i].rotation = events[j].RobotReset.to.direction
      frames.push(_.cloneDeep(state))
      state[i].alpha = 1.0
      frames.push(_.cloneDeep(state))
    } else if (events[j].PlayerFinished) {
      let i = indexByName(events[j].PlayerFinished.playerName)
      state[i].alpha = 0.0
      frames.push(_.cloneDeep(state))
    }
  }
  return frames
}

module.exports = {
  renderCanvas, robotFromPlayer, robotFromInitial, framesFromEvents
}
