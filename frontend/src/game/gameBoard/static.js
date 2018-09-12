const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

function scenarioSvg(scenario) {
  const height = svg.height(scenario)
  const width = svg.width(scenario)
  return `
<svg xmlns="http://www.w3.org/2000/svg"
  viewBox="0 0 ${width} ${height}">
  <defs>${svg.defs}</defs>
  <g transform="scale(${svg.tile}) translate(0.5 0.5)">
      ${svg.tiles(scenario)}
      ${svg.walls(scenario)}
      ${svg.target(scenario)}
      ${svg.startingPoints(scenario)}  
  </g>
</svg>`
}

function renderScenario(scenario, additionalProperties) {
  return h('div.game-board', _.merge({}, additionalProperties, {props: {innerHTML: scenarioSvg(scenario)}}))
}

module.exports = {renderScenario, scenarioSvg}
