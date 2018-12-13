package gameLogic

import gameEntities._
import org.scalatest.{FunSuite, Matchers}

class PositionSpec extends FunSuite with Matchers {

  val odd = Position(1, 1)
  val even = Position(4, 4)


  test("Up") {
    Direction.move(Up, odd) shouldBe Position(1, 0)
    Direction.move(Up, even) shouldBe Position(4, 3)
  }

  test("UpRight") {
    Direction.move(UpRight, odd) shouldBe Position(2, 1)
    Direction.move(UpRight, even) shouldBe Position(5, 3)
  }


  test("DownRight") {
    Direction.move(DownRight, odd) shouldBe Position(2, 2)
    Direction.move(DownRight, even) shouldBe Position(5, 4)
  }

  test("Down") {
    Direction.move(Down, odd) shouldBe Position(1, 2)
    Direction.move(Down, even) shouldBe Position(4, 5)
  }

  test("DownLeft") {
    Direction.move(DownLeft, odd) shouldBe Position(0, 2)
    Direction.move(DownLeft, even) shouldBe Position(3, 4)
  }

  test("UpLeft") {
    Direction.move(UpLeft, odd) shouldBe Position(0, 1)
    Direction.move(UpLeft, even) shouldBe Position(3, 3)
  }

  test("walk in a cycle") {
    def cycle(p: Position) =
      Direction.move(UpRight,
        Direction.move(DownRight,
          Direction.move(Down,
            Direction.move(DownLeft,
              Direction.move(UpLeft,
                Direction.move(Up, p))))))

    cycle(odd) shouldBe odd
    cycle(even) shouldBe even
  }
}
