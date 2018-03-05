var state;
//var player = document.body.getAttribute("player");

function start(){
}

function loadGameState() {
    return fetch("/api/game/default")
        .then(x => x.json())
        .then(defineGlobalState)
        .then(() => console.log("loaded state from server"))
        .catch(error => console.log(error))
}

loadGameState().then(draw)


var exampleSocket = new WebSocket("ws://" + (document.location.host) + "/api/events/default");

function placePiece(b, x, y, piece) {
    b.cell([y, x]).place(piece.clone())
}

function sendCommand(command) {
    fetch("/api/game/default", {
        method: "POST",
        body: JSON.stringify(command)
    }).then(resp => console.log("sendCommand", command))
}

exampleSocket.onmessage = function(event) {
    console.log("event from server", JSON.parse(event.data));
    loadGameState().then(draw);
}

function defineGlobalState(s) {
    state = s;
}

function draw() {
    drawState();
    drawActionButtons();
}

function drawState() {
    document.getElementById('game').innerHTML = '';

    var scenario = state.GameRunning.scenario
    var b = jsboard.board({
        attach: "game",
        size: scenario.height + "x" + scenario.width
    });
    b.cell("each").style({
        width: "75px",
        height: "75px"
    });

    var pieceBeacon = jsboard.piece({
        text: "B",
        fontSize: "45px",
        textAlign: "center"
    });
    var pieceTarget = jsboard.piece({
        text: "T",
        fontSize: "45px",
        textAlign: "center"
    });

    placePiece(b, scenario.beaconPosition.x, scenario.beaconPosition.y, pieceBeacon);
    placePiece(b, scenario.targetPosition.x, scenario.targetPosition.y, pieceTarget);

    state.GameRunning.players.forEach((player, index) => {
        var robot = state.GameRunning.robots[player]
        var playerPiece = jsboard.piece({
            text: '' + (index + 1),
            fontSize: "45px",
            textAlign: "center",
            title: JSON.stringify(robot)
        });
        placePiece(b, robot.position.x, robot.position.y, playerPiece)
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