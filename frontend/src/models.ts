export interface Game {
  id: string
  cycle: number
  scenario: Scenario
  robots: Robot[]
  events: EventLog[]
  you?: Player
}

export interface EventLog {
}

export interface Player {
  index: number
  name: string,
  robot: Robot
  instructionSlots: number[],
  instructionOptions: Instruction[],
  finished?: FinishedStatistic
}

export interface Scenario {
  width: number
  height: number
  targetPosition: Position
  initialRobots: Robot[]
  walls: Wall[]
  pits: Position[]
  traps: Trap[]
}

export interface Robot {
  index: number
  position: Position
  direction: Direction
}

export interface Wall{

}

export interface Position{
  x, y: number
}

export interface Direction{

}

export interface Trap{

}

export interface ScenarioRow {
  id: string
  owner: string
  description: string
  scenario: Scenario
}


export interface FinishedStatistic{

}

export interface Instruction{

}