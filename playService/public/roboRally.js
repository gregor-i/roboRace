var state;
var game;
var player;
var source;
var animationQueue = []
var playingAnimation = null

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
        .then(s => {state = s})
        .then(() => console.log("loaded state from server"))
        .catch(error => console.log(error))
}

function sendCommand(command) {
    fetch("/api/games/"+game+"/commands", {
        method: "POST",
        body: JSON.stringify(command)
    }).then(resp => console.log("sendCommand", command))
}

function eventHandler(event) {
    var json = JSON.parse(event.data);
    // console.log("event from server", json);
    document.getElementById('eventLog').innerHTML += ("<li>"+ JSON.stringify(json) +"</li>")
    /*if(json.PlayerActionsExecuted)
        loadGameState().then(draw);*/
    if(json.RobotDirectionTransition || json.RobotPositionTransition)
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

function triggerAnimation() {
    if (animationQueue.length > 0 && !playingAnimation) {
        playingAnimation = animationQueue.shift()
        if (playingAnimation.RobotDirectionTransition) {
            state.GameRunning.robots[playingAnimation.RobotDirectionTransition.playerName].direction = playingAnimation.RobotDirectionTransition.to
        } else if (playingAnimation.RobotPositionTransition) {
            state.GameRunning.robots[playingAnimation.RobotPositionTransition.playerName].position = playingAnimation.RobotPositionTransition.to
        }
        drawRobots()
        setTimeout(clearRunningAnimation, 2000)
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
            } else {
                console.log("tile not found");
            }
        })
    }
}

function drawActionButtons() {

    function action(player, cycle, action) {
        var a = {}
        a[action] = {}
        return JSON.stringify({
            "DefineNextAction": {
                "player": player,
                "cycle": cycle,
                "action": a
            }
        });
    }

    var innerHtml = ""
    if (state.GameRunning) {
        state.GameRunning.players.forEach(function(player, index){
            innerHtml += "<tr>" +
            "<td>" + player + "(" + (index + 1) + ")" + "</td>" +
            "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'MoveForward') + ")'>MoveForward</button></td>" +
            "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'MoveBackward') + ")'>MoveBackward</button></td>" +
            "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'TurnRight') + ")'>TurnRight</button></td>" +
            "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'TurnLeft') + ")'>TurnLeft</button></td>" +
            "</tr>"
        })
    }else if(state.GameNotStarted){
        innerHtml += "Game has not yet started."
    }
    document.getElementById('controls').innerHTML = innerHtml;
}
