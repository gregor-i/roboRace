package gameEntities

case class Player(index: Int,
                  name: String,
                  robot: Robot,
                  instructionSlots: Seq[Instruction],
                  instructionOptions: Seq[InstructionOption],
                  finished: Option[FinishedStatistic])

case class FinishedStatistic(rank: Int, cycle: Int, rageQuitted: Boolean)
