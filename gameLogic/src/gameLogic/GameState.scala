package gameLogic

sealed trait GameState

case class GameNotStarted(playersNames: Seq[String]) extends GameState

case class GameRunning(cycle: Int, gameMap: GameMap) extends GameState

object GameState{
  val initalState = GameNotStarted(Nil)
}