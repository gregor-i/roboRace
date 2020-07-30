package TDD

import org.scalatest.funsuite.AnyFunSuite

class SawFunctionSpec extends AnyFunSuite {
  def saw(x: Double): Double = {
    val m = x.abs % 2
    if (m > 1) 2 - m
    else m
  }

  def testSaw(in: Double, exp: Double) =
    test(s"saw($in) == $exp") {
      assert(saw(in) === exp)
    }

  testSaw(-2.0, 0.0)
  testSaw(0.0, 0.0)
  testSaw(2.0, 0.0)
  testSaw(4.0, 0.0)

  testSaw(-1.0, 1.0)
  testSaw(1.0, 1.0)
  testSaw(3.0, 1.0)
  testSaw(5.0, 1.0)

  testSaw(0.5, 0.5)
  testSaw(1.5, 0.5)
  testSaw(2.5, 0.5)
  testSaw(3.5, 0.5)
  testSaw(4.5, 0.5)
  testSaw(5.5, 0.5)
  testSaw(6.5, 0.5)
  testSaw(7.5, 0.5)
  testSaw(8.5, 0.5)

}
