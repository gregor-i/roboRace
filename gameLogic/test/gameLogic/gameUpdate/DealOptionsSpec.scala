package gameLogic.gameUpdate

import gameEntities.Constants
import org.scalatest.{FunSuite, Matchers}

class DealOptionsSpec extends  FunSuite with Matchers {
  test(s"DealOptions.initial contains exactly 12 instructions and are sorted"){
    DealOptions.initial.map(_.count).sum shouldBe 12
  }

  test("DealOptions.apply creates instructions using the weights"){
    for (_ <- 0 until 1000) {
      val options = DealOptions.apply()

      options.map(_.count).sum should be >= Constants.minimalOptionsPerCycle

      for (w <- DealOptions.weights) {
        val n = options.find(_.instruction == w.instruction).fold(0)(_.count)
        n should be <= w.max
        n should be >= w.min
      }
    }
  }
}
