var state;
var game;
var player;
var source;
var animationQueue = []
var playingAnimation = null
var robotRotations = {}

document.addEventListener("DOMContentLoaded", start)

function start() {
  player = document.body.getAttribute("player");
  game = document.body.getAttribute("game");
  loadGameState().then(draw)
  source = new EventSource("/api/games/"+game+"/events");
  source.onmessage = eventHandler
}

function loadGameState() {
    return fetch("/api/games/"+game)
        .then(x => x.json())
        .then(initalizeState)
        .then(() => console.log("loaded state from server"))
        .catch(error => console.log(error))
}

function sendCommand(command) {
    fetch("/api/games/"+game+"/commands", {
        method: "POST",
        body: JSON.stringify(command)
    }).then(resp => console.log("sendCommand", command))
}

function initalizeState(s) {
    state = s;
    state.GameRunning.players.forEach(function (player, index) {
        robotRotations[player] = nearestRotation(0, state.GameRunning.robots[player].direction)
    })
}

function directionToRotation(direction) {
    if(direction.Up)
        return 0
    else if(direction.Right)
        return 1
    else if(direction.Down)
        return 2
    else if(direction.Left)
        return 3
}

function eventHandler(event) {
    var json = JSON.parse(event.data);
    // console.log("event from server", json);
    document.getElementById('eventLog').innerHTML += ("<li>"+ JSON.stringify(json) +"</li>")
    /*if(json.PlayerActionsExecuted)
        loadGameState().then(draw);*/
    if(json.RobotDirectionTransition || json.RobotPositionTransition || json.RobotReset || PlayerFinished)
        pushAnimation(json);
    if(json.PlayerActionsExecuted) {
        state.GameRunning.cycle = json.PlayerActionsExecuted.nextCycle;
        drawActionButtons();
    }
}

function pushAnimation(animation) {
    animationQueue.push(animation)
    triggerAnimation()
}

function nearestRotation(currentRotation, newDirection){
    var newRotation = directionToRotation(newDirection)
    var c = (newRotation % 4 + 4) % 4
    var ret1 = c +Math.floor(currentRotation /4)*4
    var ret2 = c +Math.floor(currentRotation /4 -1)*4
    var ret3 = c +Math.floor(currentRotation /4 +1)*4

    var d1 = Math.abs(ret1 - currentRotation)
    var d2 = Math.abs(ret2 - currentRotation)
    var d3 = Math.abs(ret3 - currentRotation)

    var ret
    if(d1 <= d2 && d1 <= d3)
        ret = ret1
    else if(d2 <= d1 && d2 <= d3)
        ret = ret2
    else
        ret = ret3

    console.log(newRotation, currentRotation, [ret1, ret2, ret3], [d1, d2, d3], ret)
    return ret
}

function triggerAnimation() {
    if (animationQueue.length > 0 && !playingAnimation) {
        playingAnimation = animationQueue.shift()
        if (playingAnimation.RobotDirectionTransition) {
            robotRotations[playingAnimation.RobotDirectionTransition.playerName] =
                nearestRotation(
                    robotRotations[playingAnimation.RobotDirectionTransition.playerName],
                    playingAnimation.RobotDirectionTransition.to)
        } else if (playingAnimation.RobotPositionTransition) {
            state.GameRunning.robots[playingAnimation.RobotPositionTransition.playerName].position = playingAnimation.RobotPositionTransition.to
        } else if (playingAnimation.RobotReset) {
            state.GameRunning.robots[playingAnimation.RobotReset.playerName] = playingAnimation.RobotReset.to
        } else if(playingAnimation.PlayerFinished) {
            // todo
        }

        drawRobots()
        setTimeout(clearRunningAnimation, 1000)
    }
}

function clearRunningAnimation(){
    playingAnimation = null;
    triggerAnimation();
}


function draw() {
    drawRobots();
    drawActionButtons();
}

function drawRobots() {
    if(state.GameRunning) {
        var scenario = state.GameRunning.scenario;

        state.GameRunning.players.forEach(function (player, index) {
            var robot = state.GameRunning.robots[player];
            var element = document.getElementById("robot_" + player);

            if (!element)
                document.getElementById("game").innerHTML += "<robot id='robot_" + player + "' class='tile'>" + (index + 1) + "</robot>";

            element = document.getElementById("robot_" + player);
            element.className = '';
            element.classList.add('direction_' + Object.keys(robot.direction));
            element.tile = JSON.stringify(robot);

            var tile = document.getElementById("tile_" + (robot.position.x) + "_" + (robot.position.y));
            if (tile) {
                var rect = tile.getBoundingClientRect();
                element.style.top = rect.top + "px";
                element.style.left = rect.left + "px";
                element.style.width = rect.width + "px";
                element.style.height = rect.height + "px";
                element.style.transform = "rotate("+(robotRotations[player]*90)+"deg)"
            } else {
                console.log("tile not found");
            }
        })
    }
}


function actionCommand(action, slot) {
    var a = {}
    a[action] = {}
    return sendCommand({
        "DefineNextAction": {
            "player": player,
            "cycle": state.GameRunning.cycle,
            "slot" : slot,
            "action": a
        }
    });
}

function drawActionButtons() {
     function actionSelect(slot){
        return "<select onblur='actionCommand(this.value, "+slot+")'><option>null</option><option>MoveForward</option><option>MoveBackward</option><option>TurnRight</option><option>TurnLeft</option></select>"
    }

    var innerHtml = ""
    if (state.GameRunning) {
        for(var slot = 0; slot < 5; slot ++ )
            innerHtml += "<tr>" +
                "<td>slot: "+(slot+1)+"</td>" +
                "<td>" + actionSelect(slot)+"</td>" +
                "</tr>"
    }else if(state.GameNotStarted){
        innerHtml += "Game has not yet started."
    }
    document.getElementById('controls').innerHTML = innerHtml;
}
