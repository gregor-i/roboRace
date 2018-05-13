var snabbdom = require('snabbdom')
var patch = snabbdom.init([
    require('snabbdom/modules/eventlisteners').default,
    require('snabbdom/modules/props').default,
    require('snabbdom/modules/class').default,
    require('snabbdom/modules/style').default
])

var gameUi = require('./game-ui')
var gameService = require('./game-service')
var editorService = require('../editor/editor-service')
var actions = require('./game-actions')

function Game(element, player, gameId){
    var node = element

    function renderState(state) {
        state.eventSource.onmessage = gameEventHandler(state)

        window.currentState = state
        node = patch(node, gameUi(state, actionHandler(state)))
    }

    function actionHandler(state) {
        return function(action) {
            const promise = actions(state, action)
            if (promise && promise.then)
                promise.then(renderState)
        }
    }

    function gameEventHandler(state){
        return function(event){
            const data = JSON.parse(event.data)
            const newGameState = data.state
            const events = data.events
            state.game = newGameState
            state.logs = state.logs.concat(data.textLog)
            if (events.find((event) => !!event.StartNextCycle || !!event.AllPlayersFinished)) state.slots = []
            renderState(state)
        }
    }

    editorService.loadAllScenarios().then(function (scenarios) {
        return gameService.getState(gameId).then(function (game) {
            renderState({
                player, gameId, game,
                eventSource: gameService.updates(gameId),
                slots: [],
                logs: [],
                modal: 'none',
                scenarios: scenarios
            }, element)
        }).catch(function (ex) {
            document.location = '/'
        })
    }).catch(function (ex) {
        document.location = '/'
    })

    return this
}

module.exports = Game