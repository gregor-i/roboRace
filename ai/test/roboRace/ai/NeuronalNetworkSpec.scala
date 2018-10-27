package roboRace.ai

import gameLogic._
import gameLogic.gameUpdate.DealOptions
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

import scala.annotation.tailrec

class NeuronalNetworkSpec extends FunSuite with Matchers with GameUpdateHelper with BotHelper {
  test("enumerateView") {
    val p = Position(5, 4)
    NeuronalNetwork.enumerateView(p, -1) shouldBe Seq.empty
    NeuronalNetwork.enumerateView(p, 0) shouldBe Seq(p)
    NeuronalNetwork.enumerateView(p, 1).toSet shouldBe Direction.directions.map(d => d(p)).toSet + p
    NeuronalNetwork.enumerateView(p, 1).size shouldBe 7

    for(i <- 0 to 10) {
      val viewA = NeuronalNetwork.enumerateView(p, i)
      val viewB = NeuronalNetwork.enumerateView(p, i + 1)
      viewB should contain allElementsOf viewA
    }
  }

  test("enumeratedViewSize") {
    val p = Position(5, 4)
    for(i <- 0 until 10)
      NeuronalNetwork.enumerateView(p, i).size shouldBe NeuronalNetwork.enumeratedViewSize(i)
  }

  test("start with initial random genes") {
    val nn = NeuronalNetwork(NeuronalNetwork.genesFromSeed(1l))

    val thisRobot = Robot(Position(1, 9), Up)
    val scenario = Scenario(10, 10, Position(1,1), Seq(thisRobot), Seq.empty, Seq.empty, Seq.empty)

    val chosen = nn.apply(scenario, Seq.empty)(thisRobot, DealOptions.initial)

    chosen.distinct.size shouldBe chosen.size
    chosen.size shouldBe Constants.instructionsPerCycle
    chosen.foreach { i =>
      i should be >= 0
      i should be < Constants.instructionOptionsPerCycle
    }
  }

}
