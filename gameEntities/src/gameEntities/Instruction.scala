package gameEntities

sealed trait Instruction

sealed trait TurnInstruction extends Instruction
sealed trait MoveInstruction extends Instruction

case object MoveForward extends MoveInstruction
case object MoveBackward extends MoveInstruction
case object StepRight extends MoveInstruction
case object StepLeft extends MoveInstruction
case object MoveTwiceForward extends MoveInstruction

case object TurnRight extends TurnInstruction
case object TurnLeft extends TurnInstruction
case object UTurn extends TurnInstruction

case object Sleep extends Instruction

object Instruction{
  val instructions = Seq[Instruction](
    MoveForward,
    MoveTwiceForward,
    StepRight,
    StepLeft,
    MoveBackward,
    TurnRight,
    TurnLeft,
    UTurn,
    Sleep)

  implicit val ordering: Ordering[Instruction] = Ordering.Int.on[Instruction](instructions.indexOf)
}
