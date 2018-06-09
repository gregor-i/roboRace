const _ = require('lodash')
const h = require('snabbdom/h').default
const images = require('../common/images')
const constants = require('../common/constants')
const shapes = require('./shapes')

function Robot(index, x, y, rotation, alpha) {
  return {index, x, y, rotation, alpha}
}

function robotFromPlayer(player) {
  return Robot(player.index, player.robot.position.x, player.robot.position.y, directionToRotation(player.robot.direction), player.finished ? 0 : 1)
}

function robotFromInitial(initial, index){
  return Robot(index, initial.position.x, initial.position.y, directionToRotation(initial.direction), 1)
}

function interpolateRobots(r1, r2, t) {
  // https://gist.github.com/shaunlebron/8832585
  function angleLerp(a0, a1, t) {
    const da = (a1 - a0) % (Math.PI * 2)
    return a0 + (2 * da % (Math.PI * 2) - da) * t
  }

  return Robot(r1.index,
    r1.x * (1 - t) + r2.x * t,
    r1.y * (1 - t) + r2.y * t,
    angleLerp(r1.rotation, r2.rotation, t),
    r1.alpha * (1 - t) + r2.alpha * t)
}

function drawCanvas(canvas, scenario, robots) {
  const ctx = canvas.getContext("2d")

  const k = Math.sqrt(3) / 2

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
      const m = x % 2
      if (m > 1) return 2 - m
      else return m
    }

    return offsetTop + deltaTop * (y + saw(x) / 2) + tile /2
  }

  const s = shapes(tile, wallFactor)

  function centerOn(x, y, callback) {
    ctx.save()
    ctx.translate(left(x, y), top(x, y))
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
  robots.forEach(robot =>
    centerOn(robot.x, robot.y, () => {
      ctx.globalAlpha = robot.alpha
      ctx.rotate(robot.rotation)
      ctx.drawImage(images.player(robot.index), -tile / 4, -tile / 4, tile / 2, tile / 2)
    })
  )
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
    const robots = currentFrame.map((robot, index) => interpolateRobots(robot, nextFrame[index], frameProgress))
    drawCanvas(canvas, scenario, robots)
    requestAnimationFrame(() => drawAnimatedCanvas(canvas, startTime, scenario, frames, newStateRobots))
  }
}


function renderCanvas(scenario, robots, animationStart, animations) {
  return h('canvas.game-view', {
      hook: {
        postpatch: (oldVnode, newVnode) => {
          drawAnimatedCanvas(newVnode.elm, animationStart, scenario, animations, robots)
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
  if (oldGameState.GameRunning) {
    function indexByName(name) {
      return oldGameState.GameRunning.players.find((player) => player.name === name).index
    }

    let state = oldGameState.GameRunning.players.map(robotFromPlayer)
    let frames = []

    for (let j = 0; j < events.length; j++) {
      if (events[j].RobotAction) {
        frames.push(_.cloneDeep(state))
      } else if (events[j].RobotMoves) {
        let i = indexByName(events[j].RobotMoves.playerName)
        state[i].x = events[j].RobotMoves.to.x
        state[i].y = events[j].RobotMoves.to.y
      } else if (events[j].RobotTurns) {
        let i = indexByName(events[j].RobotTurns.playerName)
        state[i].rotation = directionToRotation(events[j].RobotTurns.to)
      } else if (events[j].RobotReset) {
        let i = indexByName(events[j].RobotReset.playerName)
        state[i].x = events[j].RobotReset.to.position.x
        state[i].y = events[j].RobotReset.to.position.y
        state[i].rotation = events[j].RobotReset.to.direction
      } else if (events[j].PlayerFinished) {
        let i = indexByName(events[j].PlayerFinished.playerName)
        state[i].alpha = 0.0
      }
    }
    frames.push(_.cloneDeep(state))
    return frames
  }
}

module.exports = {
  renderCanvas, robotFromPlayer, robotFromInitial, framesFromEvents
}
