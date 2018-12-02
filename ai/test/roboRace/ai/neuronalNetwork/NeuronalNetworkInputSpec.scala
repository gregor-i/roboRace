package roboRace.ai.neuronalNetwork

import gameLogic._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}
import roboRace.ai.BotHelper

class NeuronalNetworkInputSpec extends FunSuite with Matchers with GameUpdateHelper with BotHelper {
  test("enumerateView") {
    val p = Position(5, 4)
    NeuronalNetworkInput.enumerateView(-1) shouldBe Set.empty
    NeuronalNetworkInput.enumerateView(0) shouldBe Set(Seq.empty)
    NeuronalNetworkInput.enumerateView(1) shouldBe Direction.directions.map(d => Seq(d)) ++ Set(Seq.empty)
    NeuronalNetworkInput.enumerateView(1).size shouldBe 7

    for(i <- 0 to 10) {
      val viewA = NeuronalNetworkInput.enumerateView(i)
      val viewB = NeuronalNetworkInput.enumerateView(i + 1)
      viewB should contain allElementsOf viewA
    }
  }
}
