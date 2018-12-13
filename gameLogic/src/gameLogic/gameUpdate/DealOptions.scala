package gameLogic.gameUpdate

import gameEntities._

import scala.util.Random

object DealOptions {
  val initial: List[Instruction] = List(
    repeat(MoveForward, 4),
    repeat(MoveBackward, 2),
    repeat(TurnLeft, 3),
    repeat(TurnRight, 3)
  ).flatten.sorted

  def apply(): List[Instruction] = List.fill(Constants.instructionOptionsPerCycle)(choose(random.nextInt(sum))).sorted

  private def repeat(instruction: Instruction, times: Int) = List.fill(times)(instruction)

  private val random = new Random()

  def choose(input: Int): Instruction = {
    require(input >=0 && input < sum)
    var r = input
    for ((instruction, weight) <- weights)
      if (weight.randomWeight > r)
        return instruction
      else
        r -= weight.randomWeight
    throw new AssertionError()
  }

  val weights: List[(Instruction, InstructionWeights)] = List(
    MoveForward -> InstructionWeights(0, 12, 70),
    MoveBackward -> InstructionWeights(0, 12, 20),
    MoveTwiceForward -> InstructionWeights(0, 12, 5),
//    StepRight -> InstructionWeights(0, 12, 5),
//    StepLeft -> InstructionWeights(0, 12, 5),
    TurnRight -> InstructionWeights(0, 12, 30),
    TurnLeft -> InstructionWeights(0, 12, 30),
    UTurn -> InstructionWeights(0, 12, 3),
    Sleep -> InstructionWeights(0, 12, 10)
  )
  val sum = weights.map(_._2.randomWeight).sum
}

case class InstructionWeights(min: Int, max: Int, randomWeight: Int)
