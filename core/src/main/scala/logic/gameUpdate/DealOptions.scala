package logic.gameUpdate

import entities._

import scala.util.Random

object DealOptions {
  val initial: Seq[InstructionOption] =
    Instruction.instructions
      .map(instr => InstructionOption(instr, weight(instr).initial))
      .filter(_.count > 0)

  def apply(unusedInstructions: Seq[InstructionOption]): Seq[InstructionOption] =
    Instruction.instructions
      .map { instr =>
        val w        = weight(instr)
        val unused   = unusedInstructions.find(_.instruction == instr).fold(0)(_.count)
        val increase = random.nextInt(w.increaseMax - w.increaseMin + 1) + w.increaseMin
        InstructionOption(instr, Math.min(w.cap, increase + unused))
      }
      .filter(_.count > 0)

  private val random = new Random()

  def weight(instr: Instruction): InstructionWeight = instr match {
    case MoveForward      => InstructionWeight(4, 2, 3, 5)
    case MoveBackward     => InstructionWeight(2, 0, 1, 3)
    case MoveTwiceForward => InstructionWeight(0, 0, 1, 2)
    case StepRight        => InstructionWeight(0, 0, 0, 0)
    case StepLeft         => InstructionWeight(0, 0, 0, 0)
    case TurnRight        => InstructionWeight(2, 1, 1, 2)
    case TurnLeft         => InstructionWeight(2, 1, 1, 2)
    case UTurn            => InstructionWeight(0, 0, 1, 1)
    case Sleep            => InstructionWeight(0, 0, 1, 2)
  }
}

case class InstructionWeight(initial: Int, increaseMin: Int, increaseMax: Int, cap: Int)
