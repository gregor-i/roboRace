var _ = require('lodash')

function queue(oldGameState, newGameState, events) {
    if (oldGameState.GameRunning && newGameState.GameRunning) {
        var players = oldGameState.GameRunning.players

        for (var i = 0; i < players.length; i++) {
            var keyframes = []
            var element = document.getElementById('robot_' + (i + 1))
            var player = players[i]

            var oldRobot = oldGameState.GameRunning.robots[player]
            var newRobot = newGameState.GameRunning.robots[player]
            var rot = directionToRotation(oldRobot.direction)
            var x = oldRobot.position.x * 50
            var y = oldRobot.position.y * 50

            keyframes.push(keyframe(x, y, directionToRotation(oldRobot.direction), 0))

            for (var j = 0; j < events.length; j++) {
                if (events[j].RobotPositionTransition && events[j].RobotPositionTransition.playerName === player) {
                    x = 50 * events[j].RobotPositionTransition.to.x
                    y = 50 * events[j].RobotPositionTransition.to.y
                    keyframes.push(keyframe(x, y, rot, j / (1 + events.length)))
                } else if (events[j].RobotDirectionTransition && events[j].RobotDirectionTransition.playerName === player) {
                    rot = nearestRotation(rot, events[j].RobotDirectionTransition.to)
                    keyframes.push(keyframe(x, y, rot, j / (1 + events.length)))
                }
            }
            rot = nearestRotation(rot, newRobot.direction)
            keyframes.push(keyframe(newRobot.position.x * 50, newRobot.position.y * 50, rot, 1))

            if (keyframes.length !== 1) {
                console.log(keyframes)
                element.animate(keyframes, 2000)
            }
        }
    }
}

function keyframe(x, y, rot, offset) {
    return {
        transform: 'translate(' + x + 'px, ' + y + 'px) rotate(' + (rot * 90) + 'deg)',
        offset: offset
    }
}

function nearestRotation(currentRotation, newDirection) {
    var newRotation = directionToRotation(newDirection)
    var c = (newRotation % 4 + 4) % 4
    var ret1 = c + Math.floor(currentRotation / 4) * 4
    var ret2 = c + Math.floor(currentRotation / 4 - 1) * 4
    var ret3 = c + Math.floor(currentRotation / 4 + 1) * 4

    var d1 = Math.abs(ret1 - currentRotation)
    var d2 = Math.abs(ret2 - currentRotation)
    var d3 = Math.abs(ret3 - currentRotation)

    if (d1 <= d2 && d1 <= d3)
        return ret1
    else if (d2 <= d1 && d2 <= d3)
        return ret2
    else
        return ret3
}

function directionToRotation(direction) {
    if (direction.Up)
        return 0
    else if (direction.Right)
        return 1
    else if (direction.Down)
        return 2
    else if (direction.Left)
        return 3
}

module.exports = {queue}