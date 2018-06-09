package gameLogic.gameUpdate

import gameLogic.{Instruction, Constants, MoveBackward, MoveForward, MoveTwiceForward, Sleep, StepLeft, StepRight, TurnLeft, TurnRight, UTurn}

import scala.util.Random

object DealOptions {
  def apply(): List[Instruction] = List.fill(Constants.instructionOptionsPerCycle)(choose(random.nextInt(sum))).sorted

  private val random = new Random()

  def choose(input: Int): Instruction = {
    require(input >=0 && input < sum)
    var r = input
    for ((instruction, weight) <- weights)
      if (weight > r)
        return instruction
      else
        r -= weight
    throw new AssertionError()
  }

  val weights: List[(Instruction, Int)] = List(
    MoveForward -> 70,
    MoveBackward -> 20,
    MoveTwiceForward -> 5,
//    StepRight -> 5,
//    StepLeft -> 5,
    TurnRight -> 30,
    TurnLeft -> 30,
    UTurn -> 3,
    Sleep -> 10
  )
  val sum = weights.map(_._2).sum
}
