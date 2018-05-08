package gameLogic
package gameUpdate

import org.scalatest.{FunSuite, Matchers}

class MoveRobotsSpec extends FunSuite with Matchers {
  val c0 = GameRunning(0,
    GameScenario(10, 10,
      Position(9, 9),
      Position(9, 8),
      Map(
        0 -> Robot(Position(5, 5), Up),
        1 -> Robot(Position(6, 6), Up)
      ), Seq.empty, Seq.empty),
    Seq(
      Player(0, "1", Robot(Position(0, 0), Right), Seq.empty, None, Seq.empty),
      Player(1, "2", Robot(Position(1, 0), Right), Seq.empty, None, Seq.empty))
  )


  test("move a robot") {
    val newState =    MoveRobots(c0.players(1), MoveForward, c0).state
    newState.players(0) shouldBe c0.players(0)
    newState.players(1).robot.position shouldBe Position(2, 0)
  }

  test("push a robot by moving") {
    val newState = MoveRobots(c0.players(0), MoveForward, c0).state
    newState.players(0).robot.position shouldBe Position(1, 0)
    newState.players(1).robot.position shouldBe Position(2, 0)
  }

  test("don't push through walls"){
    val c0WithWall = c0.copy(scenario = c0.scenario.copy(walls = Seq(Wall(Position(1, 0), Right))))
    MoveRobots(c0.players(0), MoveForward, c0WithWall).state shouldBe c0WithWall
    MoveRobots(c0.players(1), MoveForward, c0WithWall).state shouldBe c0WithWall
  }

  test("move robots from the board") {
    val newState = MoveRobots(c0.players(1), MoveBackward, c0).state
    newState.players(0).robot shouldBe c0.scenario.initialRobots(0)
    newState.players(1).robot.position shouldBe Position(0, 0)
  }

  test("move twice without barrier"){
    val newState = MoveRobots(c0.players(1), MoveTwiceForward, c0).state
    newState.players(0) shouldBe c0.players(0)
    newState.players(1).robot.position shouldBe Position(3, 0)
  }

  test("move twice with a barrier on the way"){
    val c0WithWall = c0.copy(scenario = c0.scenario.copy(walls = Seq(Wall(Position(2, 0), Right))))
    val newState = MoveRobots(c0.players(1), MoveTwiceForward, c0WithWall).state
    newState.players(0) shouldBe c0.players(0)
    newState.players(1).robot.position shouldBe Position(2, 0)
  }

  test("move twice also pushed twice"){
    val newState = MoveRobots(c0.players(0), MoveTwiceForward, c0).state
    newState.players(0).robot.position shouldBe Position(2, 0)
    newState.players(1).robot.position shouldBe Position(3, 0)
  }
}
