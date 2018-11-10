package roboRace.ai

import gameLogic._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}
import roboRace.ai.neuronalNetwork.NeuronalNetworkInput

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

  //  test("start with initial random genes") {
//    val nn = NeuronalNetwork(NeuronalNetwork.genesFromSeed(1l))
//
//    val thisRobot = Robot(Position(1, 9), Up)
//    val scenario = Scenario(10, 10, Position(1,1), Seq(thisRobot), Seq.empty, Seq.empty, Seq.empty)
//
//    val chosen = nn.apply(scenario, Seq.empty)(thisRobot, DealOptions.initial)
//
//    chosen.distinct.size shouldBe chosen.size
//    chosen.size shouldBe Constants.instructionsPerCycle
//    chosen.foreach { i =>
//      i should be >= 0
//      i should be < Constants.instructionOptionsPerCycle
//    }
//  }

}
