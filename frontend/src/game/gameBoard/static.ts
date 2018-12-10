import {h} from 'snabbdom'
import {height, startingPoints, target, tiles, traps, walls, width} from "./svg";

export function renderScenario(scenario, tileClickListener) {
  return h('div.game-board',
    h('svg', {attrs: {xmlns: "http://www.w3.org/2000/svg", viewBox: `0 0 ${width(scenario)} ${height(scenario)}`}}, [
      h('g', tiles(scenario, tileClickListener)),
      h('g', walls(scenario)),
      h('g', target(scenario)),
      h('g', traps(scenario)),
      h('g', startingPoints(scenario))
    ]))
}
