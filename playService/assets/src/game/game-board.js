const _ = require('lodash')
const h = require('snabbdom/h').default
const images = require('../common/images')
const constants = require('../common/constants')

function Robot(index, x, y, rotation, alpha) {
  return {index, x, y, rotation, alpha}
}

function robotFromPlayer(player) {
  return Robot(player.index, player.robot.position.x, player.robot.position.y, directionToRotation(player.robot.direction), player.finished ? 0 : 1)
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

  const rect = canvas.getBoundingClientRect()
  const width = rect.width
  const height = rect.height
  canvas.width = width
  canvas.height = height

  const tileWidth = width / scenario.width
  const tileHeight = height / (scenario.height + 0.1 * (scenario.height - 1) + 0.5 + 2 * 0.1)

  const tile = Math.min(tileHeight, tileWidth)

  const wall = tile / 10
  const hexagonSideLength = tile / Math.sqrt(3)

  const offsetTop = (canvas.height - (scenario.height * tile + (scenario.height - 1) * wall + 0.5 * tile + 2 * wall)) / 2
  const offsetLeft = (canvas.width - scenario.width * tile) / 2

  function left(x, y) {
    return offsetLeft + (1.5 * hexagonSideLength) * x + x * wall
  }

  function top(x, y) {
    function saw(x) {
      const m = x % 2
      if (m > 1) return 2 - m
      else return m
    }

    return offsetTop + tile * y + y * wall + saw(x) * (tile + wall) / 2
  }

  // scenario:
  {
    const k = Math.sqrt(3) / 2

    function hexagon(x, y, callback) {
      ctx.save()
      ctx.translate(left(x, y) + hexagonSideLength, top(x, y) + hexagonSideLength)
      ctx.beginPath()
      ctx.moveTo(-hexagonSideLength, 0)
      ctx.lineTo(-0.5 * hexagonSideLength, k * hexagonSideLength)
      ctx.lineTo(0.5 * hexagonSideLength, k * hexagonSideLength)
      ctx.lineTo(hexagonSideLength, 0)
      ctx.lineTo(0.5 * hexagonSideLength, -k * hexagonSideLength)
      ctx.lineTo(-0.5 * hexagonSideLength, -k * hexagonSideLength)
      ctx.lineTo(-hexagonSideLength, 0)
      callback()
      ctx.fillStyle = 'black'
      ctx.fillText(x + ' - ' + y, -1 / 2 * hexagonSideLength + 2, -k * hexagonSideLength + 10)
      ctx.restore()
    }

    ctx.fillStyle = 'black'
    ctx.strokeRect(0, 0, width, height)


    // tiles:
    for (let y = 0; y < scenario.height; y++)
      for (let x = 0; x < scenario.width; x++) {
        hexagon(x, y, () => {
            ctx.fillStyle = 'lightgrey'
            ctx.fill()
            ctx.stroke()
          }
        )
      }

    // walls:
    ctx.fillStyle = 'black'
    const ox = wall * Math.sqrt(3) / 2
    const oy = wall / 2
    scenario.walls.forEach(function (w) {
      hexagon(w.position.x, w.position.y, () => {
        ctx.fillStyle = 'black'
        ctx.beginPath()
        if (w.direction.Down) {
          ctx.moveTo(-0.5 * hexagonSideLength, k * hexagonSideLength)
          ctx.lineTo(0.5 * hexagonSideLength, k * hexagonSideLength)
          ctx.lineTo(0.5 * hexagonSideLength, k * hexagonSideLength + wall)
          ctx.lineTo(-0.5 * hexagonSideLength, k * hexagonSideLength + wall)
        } else if (w.direction.DownRight) {
          ctx.moveTo(0.5 * hexagonSideLength, k * hexagonSideLength)
          ctx.lineTo(hexagonSideLength, 0)
          ctx.lineTo(hexagonSideLength + ox, oy)
          ctx.lineTo(0.5 * hexagonSideLength + ox, k * hexagonSideLength + oy)
        } else if (w.direction.UpRight) {
          ctx.moveTo(hexagonSideLength, 0)
          ctx.lineTo(0.5 * hexagonSideLength, -k * hexagonSideLength)
          ctx.lineTo(0.5 * hexagonSideLength + ox, -k * hexagonSideLength - oy)
          ctx.lineTo(hexagonSideLength + ox, -oy)
        } else {
          console.error("unknown wall direction")
        }
        ctx.fill()
        ctx.fillStyle = 'lightgrey'
        ctx.stroke()
      })
    })

    // target:
    {
      ctx.fillStyle = 'green'
      const x = scenario.targetPosition.x
      const y = scenario.targetPosition.y
      hexagon(x, y, () => {
        ctx.fillStyle = 'green'
        ctx.fill()
        ctx.strokeStyle = 'black';
        ctx.stroke()
        ctx.drawImage(images.target, -hexagonSideLength/2, -hexagonSideLength/2, hexagonSideLength, hexagonSideLength)
      })
    }

    // pits:
    scenario.pits.forEach(pit =>
      hexagon(pit.x, pit.y, () => {
        ctx.fillStyle = 'white';
        ctx.fill()
        ctx.strokeStyle = 'black';
        ctx.stroke()
        ctx.drawImage(images.pit, -hexagonSideLength/2, -hexagonSideLength/2, hexagonSideLength, hexagonSideLength)
      })
    )
  }


  // robots:
  robots.forEach((robot) => {
    ctx.save()
    ctx.globalAlpha = robot.alpha
    ctx.translate(left(robot.x, robot.y) + tile / 2, top(robot.x, robot.y) + tile / 2)
    ctx.rotate(robot.rotation)
    ctx.drawImage(images.player(robot.index), -hexagonSideLength / 2, -hexagonSideLength / 2, hexagonSideLength, hexagonSideLength)
    ctx.restore()
  })
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


function renderCanvas(state, scenario, robots) {
  return h('canvas.game-view', {
      hook: {
        postpatch: (oldVnode, newVnode) => {
          drawAnimatedCanvas(newVnode.elm, state.animationStart, scenario, state.animations, robots)
        },
        insert: (node) => {
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
        state[i].alpha = 1.0
      }
    }
    frames.push(_.cloneDeep(state))
    return frames
  }
}

module.exports = {
  renderCanvas, robotFromPlayer, framesFromEvents
}
