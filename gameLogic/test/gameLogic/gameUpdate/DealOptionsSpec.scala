package gameLogic.gameUpdate

import gameLogic.Constants
import org.scalatest.{FunSuite, Matchers}

class DealOptionsSpec extends  FunSuite with Matchers {
  test(s"DealOptions.initial contains exactly ${Constants.instructionOptionsPerCycle} instructions and are sorted"){
    DealOptions.initial.size shouldBe Constants.instructionOptionsPerCycle
    DealOptions.initial.sorted shouldBe DealOptions.initial
  }

  test("DealOptions.randomAction creates instructions using the weights"){
    def start(n: Int) = DealOptions.weights.take(n).map(_._2.randomWeight).sum

    for(n <- DealOptions.weights.indices)
      for(i <- start(n) until start(n+1))
        DealOptions.choose(i) shouldBe DealOptions.weights(n)._1

    DealOptions.weights.indices.flatMap(n => start(n) until start(n+1)) shouldBe (0 until DealOptions.sum)
  }
}
