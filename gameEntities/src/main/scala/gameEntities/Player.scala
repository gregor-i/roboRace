package gameEntities

import monocle.macros.Lenses

sealed trait Player {
  val index: Int
  val id: String
}

@Lenses
case class RunningPlayer(
    index: Int,
    id: String,
    robot: Robot,
    currentTarget: Int,
    instructionSlots: Seq[Instruction],
    instructionOptions: Seq[InstructionOption]
) extends Player

case class QuittedPlayer(index: Int, id: String) extends Player

case class FinishedPlayer(index: Int, id: String, rank: Int, cycle: Int) extends Player
