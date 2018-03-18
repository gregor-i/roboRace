package gameLogic

import gameLogic.action.ActionSlots

sealed trait GameState {
  def fold[A](ifNotDefined: GameNotDefined.type => A)
             (ifNotStarted: GameNotStarted => A)
             (ifRunning: GameRunning => A)
             (ifFinished: GameFinished => A): A = this match {
    case g: GameNotDefined.type => ifNotDefined(g)
    case g: GameNotStarted => ifNotStarted(g)
    case g: GameRunning => ifRunning(g)
    case g: GameFinished => ifFinished(g)
  }
}

case object GameNotDefined extends GameState

case class GameNotStarted(scenario: GameScenario,
                          playerNames: Seq[String]) extends GameState

case class GameRunning(cycle: Int,
                       players: Seq[String],
                       finishedPlayers: Seq[PlayerFinished],
                       scenario: GameScenario,
                       robots: Map[String, Robot],
                       robotActions: Map[String, ActionSlots]) extends GameState

case class GameFinished(players: Seq[PlayerFinished]) extends GameState


object GameState {
  val initalState = GameNotDefined
}