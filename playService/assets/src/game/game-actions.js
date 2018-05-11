var _ = require('lodash')
var gameService = require('./game-service')
var animations = require('./animations')
var constants = require('../common/constants')

function actions(state, action) {
    console.log(action)
    if (action.leaveGame)
        window.location.href = "/"
    else if (action.joinGame)
        return gameService.joinGame(action.joinGame, state.player)
            .then(_.constant(state))
    else if (action.readyForGame)
        return gameService.readyForGame(action.readyForGame, state.player)
            .then(_.constant(state))
    else if (action.selectScenario)
        return gameService.defineScenario(state.gameId, action.selectScenario)
            .then(_.constant(state))
    else if (action.defineAction) {
        if (!state.slots)
            state.slots = []
        var slot = action.defineAction.slot
        state.slots[slot] = action.defineAction.value
        if (_.range(constants.numberOfActionsPerCycle).every(i => state.slots[i] >= 0))
            gameService.defineAction(state.gameId, state.player, action.defineAction.cycle, state.slots)
        return Promise.resolve(state)
    } else if (action.replayAnimations) {
        if (state.animations && state.animations.length !== 0)
            animations.playAnimations(state.animations)
    } else if (action.setModal) {
        state.modal = action.setModal
        return Promise.resolve(state)
    } else {
        console.error("unknown action", action)
    }
}

module.exports = actions