package gameLogic

import monocle.Optional
import monocle.macros.Lenses
import util.OptionalWhere

sealed trait GameState

case object InitialGame extends GameState

@Lenses
case class GameRunning(cycle: Int,
                       scenario: GameScenario,
                       players: List[RunningPlayer]) extends GameState

object GameRunning{
  def player(name: String): Optional[GameRunning, RunningPlayer] = players.composeOptional(OptionalWhere.where(_.name == name))
}

@Lenses
case class GameFinished(players: List[RunningPlayer],
                        scenario: GameScenario) extends GameState
