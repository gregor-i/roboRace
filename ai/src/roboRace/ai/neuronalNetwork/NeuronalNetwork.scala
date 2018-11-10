package roboRace.ai.neuronalNetwork

import gameLogic._
import roboRace.ai._

case class NeuronalNetwork(connections: Seq[NeuronConnection]) extends Bot {
  def apply(scenario: Scenario, otherRobots: Seq[Robot], player: Player): Seq[Int] = {
    val inputActivations = NeuronalNetworkInput.calculateInputActivation(scenario, otherRobots, player)
    val outputActivations: Map[String, Double] = NeuronalNetworkCalculation.calculation(connections, inputActivations)
    NeuronalNetworkOutput.outputToInstructions(outputActivations)
  }
}

case class NeuronConnection(from: String, to: String, weight: Double, bias: Double)
