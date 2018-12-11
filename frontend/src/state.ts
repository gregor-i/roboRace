import {Game, Scenario, ScenarioRow} from "./models";

export class LobbyState {
  games: Game[]
  scenarios: ScenarioRow[]
}

// todo: this is shit
export class GameState {
  game?: Game
  scenario?: ScenarioRow
  focusedSlot?: number
}

export class EditorState {
  scenario: Scenario
  description: string
  scenarioId: string
  scenarioOwner: string
}