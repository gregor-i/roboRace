package logic.command

import entities._
import logic.{ValidateScenario, command}

object CreateGame {
  def apply(scenario: Scenario, index: Int)(player: String): CommandResponse = {
    if (!ValidateScenario.apply(scenario))
      Left(InvalidScenario)
    else if (!scenario.initialRobots.indices.contains(index))
      Left(InvalidIndex)
    else {
      val game = Game(
        cycle = 0,
        scenario = scenario,
        players = List.empty,
        events = Seq.empty
      )
      Command.registerForGame(index)(player)(game)
    }
  }
}
