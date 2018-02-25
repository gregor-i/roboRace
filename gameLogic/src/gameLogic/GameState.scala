package gameLogic

import gameLogic.action.Action
import gameLogic.command.{RegisterForGame, StartGame}
import gameLogic.processor.Processor

sealed trait GameState

case class GameNotStarted(playersNames: Seq[String]) extends GameState

case class GameRunning(cycle: Int,
                       players: Seq[String],
                       scenario: GameScenario,
                       robots: Map[String, Robot],
                       robotActions: Map[String, Action]) extends GameState

object GameState {
  val initalState = GameNotStarted(Nil)

  val cycle0 = Processor(initalState)(Seq(
    RegisterForGame(playerName = "player 1"),
    RegisterForGame(playerName = "player 2"),
    StartGame(GameScenario.default)
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