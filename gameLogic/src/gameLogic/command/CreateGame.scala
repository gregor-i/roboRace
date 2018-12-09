package gameLogic.command

import gameLogic._
import gameLogic.gameUpdate.DealOptions

object CreateGame {
  def apply(scenario: Scenario, index: Int)(player: String): CommandResponse = {
    if(!Scenario.validation(scenario))
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
      RegisterForGame(index)(player)(game)
    }
  }
}
