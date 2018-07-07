package gameLogic

import monocle.macros.Lenses

@Lenses
case class Player(index: Int,
                  name: String,
                  robot: Robot,
                  instructionSlots: Seq[Option[Int]],
                  instructionOptions: Seq[Instruction],
                  finished: Option[FinishedStatistic])

case class FinishedStatistic(rank: Int, cycle: Int, rageQuitted: Boolean)
