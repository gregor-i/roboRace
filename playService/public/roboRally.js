var state;
var player;

function start(){
    player = document.body.getAttribute("player");
    loadGameState().then(draw)
}

function animation(json){

}

function loadGameState() {
    return fetch("/api/game/default")
        .then(x => x.json())
        .then(defineGlobalState)
        .then(() => console.log("loaded state from server"))
        .catch(error => console.log(error))
}

var source = new EventSource("/api/events/default");
source.onmessage = eventHandler

function sendCommand(command) {
    fetch("/api/game/default", {
        method: "POST",
        body: JSON.stringify(command)
    }).then(resp => console.log("sendCommand", command))
}

function eventHandler(event) {
    var json = JSON.parse(event.data);
    console.log("event from server", json);
    document.getElementById('eventLog').innerHTML += ("<li>"+ JSON.stringify(json) +"</li>")
//    if(json.PlayerActionsExecuted)
//        loadGameState().then(draw);
    if(json.RobotPositionTransition)
        animation(json)
}

function defineGlobalState(s) {
    state = s;
}

function draw() {
    drawState();
    drawActionButtons();
}

function drawState() {
    var scenario = state.GameRunning.scenario

    state.GameRunning.players.forEach((player, index) => {
        var robot = state.GameRunning.robots[player]
        var element = document.getElementById("robot_"+player);
        if(!element)
            document.getElementById("game").innerHTML += "<robot id='robot_"+player+"'></robot>"
        element = document.getElementById("robot_"+player);
        element.className = ''
        element.classList.add('direction_'+Object.keys(robot.direction))
    })
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
    state.GameRunning.players.forEach((player, index) => {
        innerHtml += "<tr>" +
        "<td>" + player + "</td>"+
        "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'MoveForward') + ")'>MoveForward</button></td>"+
        "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'MoveBackward') + ")'>MoveBackward</button></td>"+
        "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'TurnRight') + ")'>TurnRight</button></td>"+
        "<td><button onclick='sendCommand(" + action(player, state.GameRunning.cycle, 'TurnLeft') + ")'>TurnLeft</button></td>"+
        "</tr>"
    })
    document.getElementById('controls').innerHTML = innerHtml;
}