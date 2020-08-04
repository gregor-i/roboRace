package gameLogic.gameUpdate

import gameEntities.{Constants, Instruction}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DealOptionsSpec extends AnyFunSuite with Matchers {

  def between(min: Int, max: Int) = be >= min and be <= max

  test("DealOptions.initial contains exactly 10 instructions and are sorted") {
    DealOptions.initial.map(_.count).sum shouldBe 10
  }

  test("DealOptions.weights is defined for all instructions and are sensible") {
    for (instr <- Instruction.instructions) {
      val w = DealOptions.weight(instr)
      w.increaseMin should between(0, 5)
      w.increaseMax should between(0, 3)
      w.initial should between(0, 4)
      w.cap should between(0, 5)
    }

  }

  test("DealOptions.apply adds new options regard to the increase") {
    for (_ <- 0 until 1000) {
      val options = DealOptions.apply(Seq.empty)

      for (instr <- Instruction.instructions) {
        val w = DealOptions.weight(instr)
        val n = options.find(_.instruction == instr).fold(0)(_.count)
        n should between(w.increaseMin, w.increaseMax)
      }
    }
  }

  test("DealOptions.apply will add new options with regard to the cap") {
    for (_ <- 0 until 1000) {
      val options = DealOptions.apply(DealOptions.initial)

      options.map(_.count).sum should be >= Constants.minimalOptionsPerCycle

      for (instr <- Instruction.instructions) {
        val w = DealOptions.weight(instr)
        val n = options.find(_.instruction == instr).fold(0)(_.count)
        n should between(w.initial, w.cap)
      }
    }
  }
}
