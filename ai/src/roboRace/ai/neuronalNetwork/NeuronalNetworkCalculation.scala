package roboRace.ai.neuronalNetwork

object NeuronalNetworkCalculation {
  def calculation(connections: Seq[NeuronConnection], inputActivation: Map[String, Double]): Map[String, Double] =
    NeuronalNetworkOutput.outputKeys.map { outputKey =>
      val activation = for {
        connection <- connections.filter(_.to == outputKey)
        fromActivation <- inputActivation.get(connection.from)
      } yield connection.weight * fromActivation + connection.bias

      (outputKey, activation.sum)
    }.toMap
}