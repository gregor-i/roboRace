// require('es6-promise/auto')
// require('es6-symbol/implement')
// require('whatwg-fetch')

var RoboRally = require('./index')

document.addEventListener('DOMContentLoaded', function(event) {
  var container = document.getElementById('robo-rally-lobby')
  var player = "p1"
  new RoboRally(container, player)
})
