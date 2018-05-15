package gameLogic

import org.scalatest.{FunSuite, Matchers}

class PositionSpec  extends FunSuite with Matchers{

  val odd = Position(1,1)
  val even = Position(4,4)


  test("Up"){
    Up(odd) shouldBe Position(1,0)
    Up(even) shouldBe Position(4,3)
  }

  test("UpRight"){
    UpRight(odd) shouldBe Position(2,1)
    UpRight(even) shouldBe Position(5,3)
  }


  test("DownRight"){
    DownRight(odd) shouldBe Position(2,2)
    DownRight(even) shouldBe Position(5,4)
  }

  test("Down") {
    Down(odd) shouldBe Position(1,2)
    Down(even) shouldBe Position(4,5)
  }

  test("DownLeft"){
    DownLeft(odd) shouldBe Position(0,2)
    DownLeft(even) shouldBe Position(3,4)
  }

  test("UpLeft"){
    UpLeft(odd) shouldBe Position(0,1)
    UpLeft(even) shouldBe Position(3,3)
  }

  test("walk in a cycle") {
    def cycle(p: Position) = UpRight(DownRight(Down(DownLeft(UpLeft(Up(p))))))
    cycle(odd) shouldBe odd
    cycle(even) shouldBe even
  }
}
