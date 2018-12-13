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
  implicit val ordering: Ordering[Instruction] = Ordering.Int.on {
    case MoveForward => 1
    case MoveTwiceForward => 2
    case StepRight => 3
    case StepLeft => 4
    case MoveBackward => 5
    case TurnRight => 6
    case TurnLeft => 7
    case UTurn => 8
    case Sleep => 9
  }

  val emptySlots = List.fill(Constants.instructionsPerCycle)(Option.empty[Int])
}
