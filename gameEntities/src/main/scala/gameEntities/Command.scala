package gameEntities

sealed trait Command

case class RegisterForGame(index: Int)                     extends Command
case object DeregisterForGame                              extends Command
case class SetInstructions(instructions: Seq[Instruction]) extends Command
case object ResetInstruction                               extends Command
