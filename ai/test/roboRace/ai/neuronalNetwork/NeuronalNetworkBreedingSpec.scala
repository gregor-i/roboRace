package roboRace.ai.neuronalNetwork

import org.scalatest.{FunSuite, Matchers}

class NeuronalNetworkBreedingSpec extends FunSuite with Matchers {
  test("addConnection always adds an connection"){
    (0 to 1000).foldLeft(NeuronalNetworkBreeding.initial){(network, i) =>
      network.connections.size shouldBe i
      NeuronalNetwork(NeuronalNetworkBreeding.addConnection(network.connections)(0L))
    }
  }

  test("removeConnection always removes just one connection"){
    val connections = Seq.fill[NeuronConnection](1000)(null)
    NeuronalNetworkBreeding.removeConnection(connections)(0L) shouldBe Seq.fill[NeuronConnection](999)(null)

    NeuronalNetworkBreeding.removeConnection(Seq.empty)(0L) shouldBe Seq.empty
  }

}
