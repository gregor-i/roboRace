package gameLogic

import gameLogic.action.Action

sealed trait GameState

case class GameNotStarted(playersNames: Seq[String]) extends GameState

case class GameRunning(cycle: Int,
                       players: Seq[String],
                       scenario: GameScenario,
                       robots: Map[String, Robot],
                       robotActions: Map[String, Action]) extends GameState

object GameState{
  val initalState = GameNotStarted(Nil)
}