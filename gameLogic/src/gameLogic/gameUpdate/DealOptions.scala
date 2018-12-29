package gameLogic.gameUpdate

import gameEntities._

import scala.util.Random

object DealOptions {
  val initial: Seq[InstructionOption] = Seq(
    InstructionOption(MoveForward, 4),
    InstructionOption(MoveBackward, 2),
    InstructionOption(TurnLeft, 3),
    InstructionOption(TurnRight, 3)
  )

  def apply(): Seq[InstructionOption] =
    weights.map {
      case InstructionWeights(min, max, instr) => InstructionOption(instr, random.nextInt(max - min) + min)
    }

  private def repeat(instruction: Instruction, times: Int) = List.fill(times)(instruction)

  private val random = new Random()

  val weights = List(
    InstructionWeights(2, 5, MoveForward),
    InstructionWeights(1, 2, MoveBackward),
    InstructionWeights(0, 1, MoveTwiceForward),
    InstructionWeights(2, 3, TurnRight),
    InstructionWeights(2, 3, TurnLeft),
    InstructionWeights(0, 1, UTurn),
    InstructionWeights(1, 2, Sleep)
  ).sortBy(_.instruction)
}

case class InstructionWeights(min: Int, max: Int, instruction: Instruction)
