const _ = require('lodash')
const h = require('snabbdom/h').default
const svg = require('./svg')

function gameSvg(game) {
  return `
<svg xmlns="http://www.w3.org/2000/svg"
  width="${svg.width(game.scenario)}"
  height="${svg.height(game.scenario)}"
  viewBox="0 0 ${svg.width(game.scenario)} ${svg.height(game.scenario)}">
  <defs>${svg.defs}</defs>
  <g transform="scale(${svg.tile}) translate(0.5 0.5)">
      ${svg.tiles(game.scenario)}
      ${svg.walls(game.scenario)}
      ${svg.target(game.scenario)}
      ${svg.startingPoints(game.scenario)}
      ${svg.robots(game)}
  </g>
</svg>`
}

function renderGame(scenario, additionalProperties) {
  return h('div', _.merge({}, additionalProperties, {props: {innerHTML: gameSvg(scenario)}}))
}

module.exports = {renderGame}
