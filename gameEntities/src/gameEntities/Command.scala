package gameEntities

sealed trait Command

case class RegisterForGame(index: Int) extends Command
case object DeregisterForGame extends Command
case class SetInstruction(cycle: Int, slot: Int, instruction: Int) extends Command
case class ResetInstruction(cycle: Int, slot: Int) extends Command