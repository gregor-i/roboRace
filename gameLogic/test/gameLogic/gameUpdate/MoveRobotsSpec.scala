package gameLogic
package gameUpdate

import org.scalatest.{FunSuite, Matchers}

class MoveRobotsSpec extends FunSuite with Matchers {
  val c0 = GameRunning(0,
    GameScenario(10, 10,
      Position(9, 9),
      Position(9, 8),
      List(
        Robot(Position(5, 5), Up),
        Robot(Position(6, 6), Up)
      ), List.empty, List.empty),
    List(
      RunningPlayer(0, "1", Robot(Position(0, 0), Right), List.empty, None, List.empty),
      RunningPlayer(1, "2", Robot(Position(1, 0), Right), List.empty, None, List.empty))
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
    val c0WithWall = c0.copy(scenario = c0.scenario.copy(walls = List(Wall(Position(1, 0), Right))))
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
    val c0WithWall = c0.copy(scenario = c0.scenario.copy(walls = List(Wall(Position(2, 0), Right))))
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
