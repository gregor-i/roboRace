package gameLogic.gameUpdate

import gameLogic.{Game, Instruction, InvalidScenario, Player, Scenario}

object CreateGame {
  def apply(scenario: Scenario)(player: String): CommandResponse = {
    if (Scenario.validation(scenario))
      CommandAccepted(Game(
        cycle = 0,
        scenario = scenario,
        players = List(
          Player(index = 0,
            name = player,
            robot = scenario.initialRobots(0),
            instructionSlots = Instruction.emptySlots,
            instructionOptions = DealOptions.initial,
            finished = None
          )
        ),
        events = Seq.empty
      ))
    else
      CommandRejected(InvalidScenario)
  }
}
