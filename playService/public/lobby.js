function reload() {
    document.location.reload()
}

function newGame(){
    fetch("/api/games",{
        method: "POST"
    }).then(reload)
}

function deleteGame(game){
    fetch("/api/games/"+game,{
        method: "DELETE"
    }).then(reload)
}

function sendCommand(game, command) {
    return fetch("/api/games/"+game+"/commands", {
        method: "POST",
        body: JSON.stringify(command)
    })
}

function joinGame(game){
    var playerName = prompt("enter player name:", "")
    sendCommand(game, {"RegisterForGame" : {"playerName": playerName}})
        .then(reload)
}

function startGame(game) {
    sendCommand(game, {"StartGame": {}}).then(reload)
}

function defineGame(game){
    sendCommand(game, {"DefineScenario":{
            "scenario" : {
                "width" : 7,
                "height" : 9,
                "beaconPosition" : {
                    "x" : 3,
                    "y" : 8
                },
                "targetPosition" : {
                    "x" : 3,
                    "y" : 1
                },
                "initialRobots" : {
                    "4" : {
                        "position" : {
                            "x" : 0,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    },
                    "5" : {
                        "position" : {
                            "x" : 6,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    },
                    "1" : {
                        "position" : {
                            "x" : 5,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    },
                    "0" : {
                        "position" : {
                            "x" : 1,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    },
                    "2" : {
                        "position" : {
                            "x" : 2,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    },
                    "3" : {
                        "position" : {
                            "x" : 4,
                            "y" : 8
                        },
                        "direction" : {
                            "Up" : {

                            }
                        }
                    }
                },
                "walls" : [
                ]
            }
        }}).then(reload)
}
