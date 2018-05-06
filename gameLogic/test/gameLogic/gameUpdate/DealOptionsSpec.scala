package gameLogic.gameUpdate

import org.scalatest.{FunSuite, Matchers}

class DealOptionsSpec extends  FunSuite with Matchers {
  test("DealOptions.randomAction creates actions using the weights"){
    def start(n: Int) = DealOptions.weights.take(n).map(_._2).sum

    for(n <- DealOptions.weights.indices)
      for(i <- start(n) until start(n+1))
        DealOptions.choose(i) shouldBe DealOptions.weights(n)._1

    DealOptions.weights.indices.flatMap(n => start(n) until start(n+1)) shouldBe (0 until DealOptions.sum)
  }
}
