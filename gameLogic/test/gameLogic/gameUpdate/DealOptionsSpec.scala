package gameLogic.gameUpdate

import gameEntities.Constants
import org.scalatest.{FunSuite, Matchers}

class DealOptionsSpec extends  FunSuite with Matchers {
  test(s"DealOptions.initial contains exactly 12 instructions and are sorted"){
    DealOptions.initial.size shouldBe 12
    DealOptions.initial.sorted shouldBe DealOptions.initial
  }

  test("DealOptions.apply creates instructions using the weights"){
    for (_ <- 0 until 1000) {
      val options = DealOptions.apply()

      options.size should be >= Constants.minimalOptionsPerCycle
      options shouldBe options.sorted

      for (w <- DealOptions.weights) {
        val n = options.count(_ == w.instruction)
        n should be <= w.max
        n should be >= w.min
      }
    }
  }
}
