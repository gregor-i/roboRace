import {Game, Scenario, ScenarioRow} from "./models";

export class LobbyState {
  games: Game[]
  scenarios: ScenarioRow[]
}

export class GameState {
  game: Game
  focusedSlot?: number
}

export class PreviewState{
  scenarioRow: ScenarioRow
}

export class EditorState {
  scenario: Scenario
  description: string
  scenarioId: string
  scenarioOwner: string
}