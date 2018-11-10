package roboRace.ai.neuronalNetwork

import gameLogic.Constants

object NeuronalNetworkOutput {
  def activationWeight(i: Int): String = s"activation weight $i"

  def orderWeight(i: Int): String = s"order weight $i"

  val outputKeys: Set[String] = (for {
    i <- 0 until Constants.instructionOptionsPerCycle
    outputs <- Set(activationWeight(i), orderWeight(i))
  } yield outputs).toSet

  def outputToInstructions(activations: Map[String, Double]): Seq[Int] = {
    (0 until Constants.instructionOptionsPerCycle)
      .map(i => (i, activations(activationWeight(i)), activations(orderWeight(i))))
      .sortBy(_._2)
      .take(Constants.instructionsPerCycle)
      .sortBy(_._3)
      .map(_._1)
  }
}