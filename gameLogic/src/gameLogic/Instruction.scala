package gameLogic

sealed trait Instruction

sealed trait TurnInstruction extends Instruction
sealed trait MoveInstruction extends Instruction

case object MoveForward extends MoveInstruction
case object MoveTwiceForward extends MoveInstruction
case object StepRight extends MoveInstruction
case object StepLeft extends MoveInstruction
case object MoveBackward extends MoveInstruction

case object TurnRight extends TurnInstruction
case object TurnLeft extends TurnInstruction
case object UTurn extends TurnInstruction

case object Sleep extends Instruction

object Instruction{
  def instructions = Seq(MoveForward, MoveTwiceForward, StepRight, StepLeft, MoveBackward, TurnRight, TurnLeft, UTurn, Sleep)
  def ordinal(instruction: Instruction): Int =  instructions.indexWhere(_ == instruction)

  implicit val ordering: Ordering[Instruction] = Ordering.Int.on(ordinal)

  val emptySlots = List.fill(Constants.instructionsPerCycle)(Option.empty[Int])
}
