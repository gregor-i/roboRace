var _ = require('lodash')

function animations(oldGameState, events) {
    if (oldGameState.GameRunning) {
        var animations = []
        var players = oldGameState.GameRunning.players

        for (var i = 0; i < players.length; i++) {
            var keyframes = []
            var player = players[i]

            var rot = directionToRotation(player.robot.direction)
            var x = player.robot.position.x
            var y = player.robot.position.y
            var finished = !!player.finished

            keyframes.push(keyframe(x, y, rot, finished, 0))
            for (var j = 0; j < events.length; j++) {
                var offset = (j+1) / (2 + events.length)
                if (events[j].RobotMoves && events[j].RobotMoves.playerName === player.name) {
                    x = events[j].RobotMoves.to.x
                    y = events[j].RobotMoves.to.y
                    keyframes.push(keyframe(x, y, rot, finished, offset))
                } else if (events[j].RobotTurns && events[j].RobotTurns.playerName === player.name) {
                    rot = nearestRotation(rot, events[j].RobotTurns.to)
                    keyframes.push(keyframe(x, y, rot, finished, offset))
                }else if(events[j].RobotReset && events[j].RobotReset.playerName === player.name){
                    x = events[j].RobotReset.to.position.x
                    y = events[j].RobotReset.to.position.y
                    rot = nearestRotation(rot, events[j].RobotReset.to.direction)
                    keyframes.push(keyframe(x, y, rot, finished, offset))
                } else if (events[j].PlayerFinished && events[j].PlayerFinished.playerName === player.name) {
                    finished = true
                    keyframes.push(keyframe(x, y, rot, finished, offset))
                }
            }

            keyframes.push(keyframe(x, y, rot, finished, 1))

            animations.push({element: 'robot_' + (i + 1), keyframes: keyframes})
        }
        return animations
    }
}

function playAnimations(animations) {
    for(var i=0; i<animations.length; i++){
        document.getElementById(animations[i].element).animate(animations[i].keyframes, 2000)
    }
}

function keyframe(x, y, rot, finished, offset) {
    return {
        transform: 'translate(' + (50 * x) + 'px, ' + (50 * y) + 'px) rotate(' + (rot * 90) + 'deg)',
        opacity: finished ? 0 : 1,
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

module.exports = {animations, playAnimations}