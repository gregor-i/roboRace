package gameLogic.gameUpdate

import gameLogic._

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
      ).log(PlayerJoinedGame(0, scenario.initialRobots(0))))
    else
      CommandRejected(InvalidScenario)
  }
}
