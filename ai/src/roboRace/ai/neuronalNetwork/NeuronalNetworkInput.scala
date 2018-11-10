package roboRace.ai.neuronalNetwork

import gameLogic._
import gameLogic.gameUpdate.ScenarioEffects
import gameLogic.util.PathFinding

object NeuronalNetworkInput {

  sealed abstract class Input(val label: String) {
    def activation(scenario: Scenario, player: Player, pathing: Map[Position, List[Direction]]): Double
  }

  def enumerateView(distance: Int): Set[Seq[Direction]] =
    for {
      d <- Direction.directions
      rd = d.right
      i <- 0 to distance
      j <- 0 to distance - i
    } yield Seq.fill(j)(rd) ++ Seq.fill(i)(d)

  val view = enumerateView(Constants.instructionsPerCycle * 2)

  private def applyAllDirections(directions: Seq[Direction], pos: Position): Position =
    directions.foldLeft(pos)((a, b) => b(a))

  val inputTemplates: Set[Input] = {
    def boolToWeight(s: Boolean): Double = if (s) 1d else 0d

    val pits: Set[Input] = view.map(directions =>
      new Input(s"isPit (${directions.mkString(" ")})") {
        override def activation(scenario: Scenario, player: Player, pathing: Map[Position, List[Direction]]): Double =
          boolToWeight(ScenarioEffects.isPit(scenario, applyAllDirections(directions, player.robot.position)))
      }
    )

    val distances: Set[Input] = view.map(directions =>
      new Input(s"distance (${directions.mkString(" ")})") {
        override def activation(scenario: Scenario, player: Player, pathing: Map[Position, List[Direction]]): Double =
          pathing.get(applyAllDirections(directions, player.robot.position)).fold(1000d)(_.length)
      }
    )

    val instruction: Set[Input] = (for {
      index <- 0 until Constants.instructionOptionsPerCycle
      instruction <- Instruction.instructions
    } yield new Input(s"instruction option $index is $instruction") {
      override def activation(scenario: Scenario, player: Player, pathing: Map[Position, List[Direction]]): Double =
        boolToWeight(player.instructionOptions(index) == instruction)
    }).toSet

    pits ++ distances ++ instruction
  }

  val inputKeys = inputTemplates.map(_.label)

  def calculateInputActivation(scenario: Scenario, otherRobots: Seq[Robot], player: Player): Map[String, Double] = {
    val pathing = PathFinding.toTarget(scenario)
    inputTemplates.map(input => (input.label, input.activation(scenario, player, pathing))).toMap
  }
}