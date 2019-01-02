package gameLogic

import gameEntities._
import monocle.Traversal
import org.scalatest.{FunSuite, Matchers}

class LensesSpec extends FunSuite with Matchers {
  test("self written lenses work") {
    val p = RunningPlayer(
      index = 0,
      id = "id",
      robot = Robot(
        index = 0,
        position = Position(0, 0),
        direction = Up
      ),
      currentTarget = 0,
      instructionSlots = Seq.empty,
      instructionOptions = Seq.empty
    )

    PlayerLenses.running.getOption(p) shouldBe Some(p)
    PlayerLenses.running.modify(_.copy(index = 2))(p) shouldBe p.copy(index = 2)
  }
}
