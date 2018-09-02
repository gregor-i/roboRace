package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class MoveRobotsSpec extends FunSuite with Matchers with GameUpdateHelper {
  val scenario = Scenario(10, 10,
    Position(9, 8),
    List(
      Robot(Position(5, 5), DownRight),
      Robot(Position(6, 6), DownLeft)
    ), List.empty, List.empty)

  val initialGame = sequenceWithAutoCycle(createGame(scenario)(p0))(
    RegisterForGame(p1).accepted,
    placeRobot(p0, Robot(Position(0, 0), Down)),
    placeRobot(p1, Robot(Position(0, 1), Down)),
    clearHistory
  )

  test("move a robot") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveForward),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
      ))))
    )
  }

  test("push a robot by moving") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveForward),
      forcedInstructions(p1)(),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 1)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1)),
        RobotPositionTransition(1, Down,Position(0, 1), Position(0, 2))
      ))))
    )
  }

  test("don't push through walls") {
    sequenceWithAutoCycle(initialGame)(
      addWall(Wall(Position(0, 1), Down)),
      forcedInstructions(p0)(MoveForward),
      forcedInstructions(p1)(MoveForward),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 1)),
      assertLog(_ should contain(MovementBlocked(0, Robot(Position(0, 0), Down)))),
      assertLog(_ should contain(MovementBlocked(1, Robot(Position(0, 1), Down))))
    )
  }

  test("move robots from the board") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveBackward),
      forcedInstructions(p1)(),
      assertPlayer(p0)(_.robot.position shouldBe Position(5, 5)),
      assertPlayer(p0)(_.robot.direction shouldBe DownRight),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 1)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(0, Down, Position(0, 0), Position(0, -1))
      )))),
      assertLog(_ should contain(RobotReset(0, Robot(Position(0, -1), Down), Robot(Position(5, 5), DownRight))))
    )
  }

  test("move twice without barrier") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 3)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
      )))),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(1, Down, Position(0, 2), Position(0, 3))
      ))))
    )
  }

  test("move twice with a barrier on the way") {
    sequenceWithAutoCycle(initialGame)(
      addWall(Wall(Position(0, 2), Down)),
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
      )))),
      assertLog(_ should contain(MovementBlocked(1, Robot(Position(0, 2), Down))))
    )
  }

  test("move twice also pushes twice") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveTwiceForward),
      forcedInstructions(p1)(),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 2)),
      assertPlayer(p1)(_.robot.position shouldBe Position(0, 3)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1)),
        RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
      )))),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(0, Down, Position(0, 1), Position(0, 2)),
        RobotPositionTransition(1, Down, Position(0, 2), Position(0, 3))
      ))))
    )
  }

  test("move twice stops after the first move if the robot was resetted") {
    sequenceWithAutoCycle(initialGame)(
      addPit(Position(0, 2)),
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertPlayer(p1)(_.robot.position shouldBe Position(6, 6)),
      assertLog(_ should contain(RobotMoves(Seq(
        RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
      )))),
      assertLog(_ should contain(RobotReset(1, Robot(Position(0,2),Down), Robot(Position(6,6), DownLeft))))
    )
  }
}
