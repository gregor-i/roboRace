package gameLogic

import gameLogic.action.Action
import gameLogic.command.{RegisterForGame, StartGame}
import gameLogic.processor.Processor

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
                       scenario: GameScenario,
                       robots: Map[String, Robot],
                       robotActions: Map[String, Action]) extends GameState

case class GameFinished() extends GameState


object GameState {
  val initalState = GameNotDefined

  val cycle0 = Processor(initalState)(Seq(
    RegisterForGame(playerName = "player 1"),
    RegisterForGame(playerName = "player 2"),
    StartGame
  )).state

  val ingame = GameRunning(
    cycle = 3,
    players = Seq("player 1", "player 2"),
    scenario = GameScenario.default,
    robots = Map(
      "player 1" -> Robot(Position(2, 3), Up),
      "player 2" -> Robot(Position(1, 5), Right)
    ),
    robotActions = Map.empty
  )
}