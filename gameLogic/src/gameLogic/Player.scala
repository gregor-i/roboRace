package gameLogic

import monocle.macros.Lenses

sealed trait Player {
  val index: Int
  val name: String
}

@Lenses
case class RunningPlayer(index: Int,
                         name: String,
                         robot: Robot,
                         instructions: Seq[Instruction],
                         instructionOptions: Seq[Instruction],
                         finished: Option[FinishedStatistic]) extends Player

@Lenses
case class StartingPlayer(index: Int,
                          name: String,
                          ready: Boolean) extends Player

case class FinishedStatistic(rank: Int, cycle: Int, rageQuitted: Boolean)
