package logic
package gameUpdate

import entities._
import helper.GameUpdateHelper
import logic.command.RegisterForGame
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MoveRobotsSpec extends AnyFunSuite with Matchers with GameUpdateHelper {
  val scenario = Scenario(
    10,
    10,
    Seq(Position(9, 8)),
    List(
      Robot(0, Position(5, 5), DownRight),
      Robot(1, Position(6, 6), DownLeft)
    ),
    List.empty,
    List.empty
  )

  val initialGame = sequenceWithAutoCycle(createGame(scenario)(p0))(
    RegisterForGame(1)(p1).accepted,
    forceRobot(p0, Robot(0, Position(0, 0), Down)),
    forceRobot(p1, Robot(1, Position(0, 1), Down)),
    clearHistory
  )

  test("move a robot") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveForward),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      )
    )
  }

  test("push a robot by moving") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveForward),
      forcedInstructions(p1)(),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 1)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1)),
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      )
    )
  }

  test("don't push through walls") {
    sequenceWithAutoCycle(initialGame)(
      addWall(Wall(Position(0, 1), Down)),
      forcedInstructions(p0)(MoveForward),
      forcedInstructions(p1)(MoveForward),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 1)),
      assertLog(_ should contain(MovementBlocked(0, Robot(0, Position(0, 0), Down)))),
      assertLog(_ should contain(MovementBlocked(1, Robot(1, Position(0, 1), Down))))
    )
  }

  test("move robots from the board") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveBackward),
      forcedInstructions(p1)(),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(5, 5)),
      assertRunningPlayer(p0)(_.robot.direction shouldBe DownRight),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 1)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(0, Down, Position(0, 0), Position(0, -1))
            )
          )
        )
      ),
      assertLog(_ should contain(RobotReset(0, Robot(0, Position(0, -1), Down), Robot(0, Position(5, 5), DownRight))))
    )
  }

  test("move twice without barrier") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 3)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      ),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(1, Down, Position(0, 2), Position(0, 3))
            )
          )
        )
      )
    )
  }

  test("move twice with a barrier on the way") {
    sequenceWithAutoCycle(initialGame)(
      addWall(Wall(Position(0, 2), Down)),
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 2)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      ),
      assertLog(_ should contain(MovementBlocked(1, Robot(1, Position(0, 2), Down))))
    )
  }

  test("move twice also pushes twice") {
    sequenceWithAutoCycle(initialGame)(
      forcedInstructions(p0)(MoveTwiceForward),
      forcedInstructions(p1)(),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 2)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(0, 3)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1)),
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      ),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(0, Down, Position(0, 1), Position(0, 2)),
              RobotPositionTransition(1, Down, Position(0, 2), Position(0, 3))
            )
          )
        )
      )
    )
  }

  test("move twice stops after the first move if the robot was resetted") {
    sequenceWithAutoCycle(initialGame)(
      addPit(Position(0, 2)),
      forcedInstructions(p0)(),
      forcedInstructions(p1)(MoveTwiceForward),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 0)),
      assertRunningPlayer(p1)(_.robot.position shouldBe Position(6, 6)),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(1, Down, Position(0, 1), Position(0, 2))
            )
          )
        )
      ),
      assertLog(_ should contain(RobotReset(1, Robot(1, Position(0, 2), Down), Robot(1, Position(6, 6), DownLeft))))
    )
  }

  test("robots which have reached the target will not be pushed") {
    sequenceWithAutoCycle(initialGame)(
      Lenses.player(p1).modify(p => FinishedPlayer(p.index, p.id, 0, 0)),
      forcedInstructions(p0)(MoveForward),
      assertCycle(1),
      assertRunningPlayer(p0)(_.robot.position shouldBe Position(0, 1)),
      assertFinishedPlayer(p1)(_ => succeed),
      assertLog(
        _ should contain(
          RobotMoves(
            Seq(
              RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1))
            )
          )
        )
      )
    )
  }
}
