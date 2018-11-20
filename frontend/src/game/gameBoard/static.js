const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

function renderScenario(scenario, tileClickListener) {
  const height = svg.height(scenario)
  const width = svg.width(scenario)
  return h('div.game-board',
    h('svg', {attrs: {xmlns: "http://www.w3.org/2000/svg", viewBox: `0 0 ${width} ${height}`}}, [
      h('g', svg.tiles(scenario, tileClickListener)),
      h('g', svg.walls(scenario)),
      h('g', svg.target(scenario)),
      h('g', svg.traps(scenario)),
      h('g', svg.startingPoints(scenario))
    ]))
}

module.exports = {renderScenario}
