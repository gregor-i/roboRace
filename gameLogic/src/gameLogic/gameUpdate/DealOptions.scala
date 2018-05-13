package gameLogic.gameUpdate

import gameLogic.{Action, Constants, MoveBackward, MoveForward, MoveTwiceForward, Sleep, StepLeft, StepRight, TurnLeft, TurnRight, UTurn}

import scala.util.Random

object DealOptions {
  def apply(): List[Action] = List.fill(Constants.actionOptionsPerCycle)(choose(random.nextInt(sum))).sorted

  private val random = new Random()

  def choose(input: Int): Action = {
    require(input >=0 && input < sum)
    var r = input
    for ((action, weight) <- weights)
      if (weight > r)
        return action
      else
        r -= weight
    throw new AssertionError()
  }

  val weights: List[(Action, Int)] = List(
    MoveForward -> 70,
    MoveBackward -> 20,
    MoveTwiceForward -> 5,
    StepRight -> 5,
    StepLeft -> 5,
    TurnRight -> 30,
    TurnLeft -> 30,
    UTurn -> 3,
    Sleep -> 10
  )
  val sum = weights.map(_._2).sum
}
