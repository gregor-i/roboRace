var _ = require('lodash')
var h = require('snabbdom/h').default
var images = require('../common/images')
var constants = require('../common/constants')

function Robot(index, x, y, rotation, alpha) {
    return {index, x, y, rotation, alpha}
}

function robotFromPlayer(player) {
    return Robot(player.index, player.robot.position.x, player.robot.position.y, directionToRotation(player.robot.direction), player.finished ? 0 : 1)
}

function interpolateRobots(r1, r2, t){
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
    canvas.width = rect.width
    canvas.height = rect.height

    const tileWidth = rect.width / (scenario.width * 1.1 - 0.1)
    const tileHeight = rect.height / (scenario.height * 1.1 - 0.1)

    const tile = Math.min(tileHeight, tileWidth)
    const wall = tile / 10

    const offsetLeft = (canvas.width - (scenario.width * tile + (scenario.width - 1) * wall)) / 2
    const offsetTop = (canvas.height - (scenario.height * tile + (scenario.height - 1) * wall)) / 2

    function left(x) {
        return offsetLeft + (tile + wall) * x
    }

    function top(y) {
        return offsetTop + (tile + wall) * y
    }

    // scenario:
    {
        // tiles:
        ctx.fillStyle = 'lightgrey'
        for (let y = 0; y < scenario.height; y++)
            for (let x = 0; x < scenario.width; x++)
                ctx.fillRect(left(x), top(y), tile, tile)

        // walls:
        ctx.fillStyle = 'black'
        scenario.walls.forEach(function (w) {
            if (w.direction.Right)
                ctx.fillRect(left(w.position.x) + tile, top(w.position.y), wall, tile)
            else if (w.direction.Down)
                ctx.fillRect(left(w.position.x), top(w.position.y) + tile, tile, wall)
        })

        // target:
        {
            ctx.fillStyle = 'green'
            ctx.fillRect(left(scenario.targetPosition.x), top(scenario.targetPosition.y), tile, tile)
        }

        // pits:
        ctx.fillStyle = 'white'
        scenario.pits.forEach(pit =>
            ctx.fillRect(left(pit.x), top(pit.y), tile, tile)
        )
    }


    // robots:
    robots.forEach((robot) => {
        ctx.save()
        ctx.globalAlpha = robot.alpha
        ctx.translate(left(robot.x) + tile / 2, top(robot.y) + tile / 2)
        ctx.rotate(robot.rotation)
        ctx.drawImage(images.player(robot.index), -tile / 2, -tile / 2, tile, tile)
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
    else if (direction.Right)
        return 0.5 * Math.PI
    else if (direction.Down)
        return Math.PI
    else if (direction.Left)
        return 1.5 * Math.PI
    else
        throw new Error("unknown direction")
}

function framesFromEvents(oldGameState, events) {
    if (oldGameState.GameRunning) {
        function indexByName(name){
            return oldGameState.GameRunning.players.find((player) => player.name === name).index
        }

        let state = oldGameState.GameRunning.players.map(robotFromPlayer)
        let frames = []

        for (let j = 0; j < events.length; j++) {
            if(events[j].RobotAction) {
                frames.push(_.cloneDeep(state))
            } else if (events[j].RobotMoves) {
                let i = indexByName(events[j].RobotMoves.playerName)
                state[i].x = events[j].RobotMoves.to.x
                state[i].y = events[j].RobotMoves.to.y
            } else if (events[j].RobotTurns) {
                let i = indexByName(events[j].RobotTurns.playerName)
                state[i].rotation = directionToRotation(events[j].RobotTurns.to)
            }else if(events[j].RobotReset){
                let i =  indexByName(events[j].RobotReset.playerName)
                state[i].x = events[j].RobotReset.to.position.x
                state[i].y = events[j].RobotReset.to.position.y
                state[i].rotation = events[j].RobotReset.to.direction
            } else if (events[j].PlayerFinished) {
                let i =  indexByName(events[j].PlayerFinished.playerName)
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