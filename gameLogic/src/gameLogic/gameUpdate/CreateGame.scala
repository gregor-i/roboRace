package gameLogic.gameUpdate

import gameLogic.{Game, Instruction, InvalidScenario, RunningPlayer, Scenario}

object CreateGame {
  def apply(scenario: Scenario)(player: String): CommandResponse = {
    if (Scenario.validation(scenario))
      CommandAccepted(Game(
        cycle = 0,
        scenario = scenario,
        players = List(
          RunningPlayer(index = 0,
            name = player,
            robot = scenario.initialRobots(0),
            instructionSlots = Instruction.emptySlots,
            instructionOptions = DealOptions.initial,
            finished = None
          )
        )
      ))
    else
      CommandRejected(InvalidScenario)
  }
}
