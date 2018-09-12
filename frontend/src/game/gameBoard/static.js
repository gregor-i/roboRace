const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

function renderScenario(scenario, tileClickListener) {
  const height = svg.height(scenario)
  const width = svg.width(scenario)
  return h('div.game-board',
    h('svg', {attrs: {xmlns: "http://www.w3.org/2000/svg", viewBox: `0 0 ${width} ${height}`}}, [
      svg.defs,
      h('g', {attrs: {transform: "translate(0.5 0.5)"}}, svg.tiles(scenario, tileClickListener)),
      h('g', {attrs: {transform: "translate(0.5 0.5)"}}, svg.walls(scenario)),
      h('g', {attrs: {transform: "translate(0.5 0.5)"}}, svg.target(scenario)),
      h('g', {attrs: {transform: "translate(0.5 0.5)"}}, svg.startingPoints(scenario))
    ]))
}

module.exports = {renderScenario}
