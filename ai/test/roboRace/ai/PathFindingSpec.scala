package roboRace.ai

import gameLogic._
import gameLogic.util.PathFinding
import org.scalatest.{FunSuite, Matchers}

class PathFindingSpec extends FunSuite with Matchers {
  test("find the right direction for all neighbors of the target in an empty scenario") {
    val target = Position(2, 2)
    val scenario = Scenario(5, 5, target, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
    val pathing = PathFinding.toTarget(scenario)
    for(direction <- Direction.directions)
        pathing.get(direction(target)) shouldBe Some(List(direction.back))
    pathing.get(target) shouldBe Some(List.empty)
 }

  test("find a way leading to the target from each position"){
    val target = Position(2, 2)
    val scenario = Scenario(5, 5, target, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
    val pathing = PathFinding.toTarget(scenario)

    pathing.keySet.size shouldBe 5*5

    for(pos <- pathing.keySet) {
      val path = pathing(pos)
      path match{
        case head :: tail => pathing(head(pos)) shouldBe tail
        case Nil => pos shouldBe target
      }
    }
  }

  test("find a way around pits") {
    val target = Position(2, 2)
    val scenario = Scenario(5, 5, target, Seq.empty, Seq.empty, Seq(Down(target), DownRight(target)), Seq.empty)
    val pathing = PathFinding.toTarget(scenario)
    pathing.get(Down(target)) shouldBe None
    pathing.get(DownRight(target)) shouldBe None

    pathing(Down(Down(target))) shouldBe List(UpLeft, Up, UpRight)
  }

  test("find a way around walls (WallDirection)") {
    val target = Position(2, 2)
    val scenario = Scenario(5, 5, target, Seq.empty, Seq(Wall(target, Down), Wall(target, DownRight)), Seq.empty, Seq.empty)
    val pathing = PathFinding.toTarget(scenario)
    pathing(Down(target)) shouldBe List(UpLeft, UpRight)
    pathing(DownRight(target)) shouldBe List(Up, DownLeft)
  }

  test("find a way around walls (non WallDirection)") {
    val target = Position(2, 2)
    val scenario = Scenario(5, 5, target, Seq.empty, Seq(Wall(Up(target), Down), Wall(UpLeft(target), DownRight)), Seq.empty, Seq.empty)
    val pathing = PathFinding.toTarget(scenario)
    pathing(Up(target)) shouldBe List(DownRight, DownLeft)
    pathing(UpLeft(target)) shouldBe List(Down, UpRight)
  }
}
