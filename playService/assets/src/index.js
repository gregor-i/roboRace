var snabbdom = require('snabbdom')
var render = require('./ui/index')
// var update = require('./update')
// var Result = require('./result')
var service = require('./service')

var patch = snabbdom.init([
  require('snabbdom/modules/eventlisteners').default,
  require('snabbdom/modules/props').default,
  require('snabbdom/modules/class').default,
])

function Game(element, gameId, player) {
  var init = function(gameId, player, backendState) {
    return {
      gameId : gameId,
      player: player,
      backendState: backendState
    }
  }

  var updateCallback = function(action) {
    var result = update(action, oldState)
    Result.case({
      Sync: function(newState) {
        newState.error = null
        main(newState, vnode)
      },
      Async: function(promise) {
        promise.then(function(newState) {
          newState.error = null
          main(newState, vnode)
        }).catch(function(err) {
          oldState.error = err
          main(oldState, vnode)
        })
      },
    }, result)
  }

  var node = element
  var main = function(oldState) {
    var vnode = render(oldState, updateCallback)
    node = patch(node, vnode)
  }

  service.getState(gameId).then(function(state) {
    var state = init(gameId, player)
    main(state, element)
  })

  return this
}

module.exports = Game
