package gameLogic.command

import gameEntities._
import gameLogic.ValidateScenario

object CreateGame {
  def apply(scenario: Scenario, index: Int)(player: String): CommandResponse = {
    if(!ValidateScenario.apply(scenario))
      CommandRejected(InvalidScenario)
    else if(!scenario.initialRobots.indices.contains(index))
      CommandRejected(InvalidIndex)
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
