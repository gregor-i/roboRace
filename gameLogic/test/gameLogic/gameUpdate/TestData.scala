package gameLogic.gameUpdate

import gameLogic.{GameScenario, GameStarting, StartingPlayer}

trait TestData {
  val p1 = "0"
  val p2 = "1"
  val s = GameScenario.default

  def startingStateHelper(readyStates: Boolean*) = GameStarting(s,
    (for ((ready, index) <- readyStates.zipWithIndex)
      yield StartingPlayer(index, index.toString, ready)).toList
  )
}
