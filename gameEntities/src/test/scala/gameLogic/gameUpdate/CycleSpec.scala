package gameLogic
package gameUpdate

import gameEntities._
import helper.GameUpdateHelper
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CycleSpec extends AnyFunSuite with Matchers with GameUpdateHelper {
  test("should execute actions if all players are entered their action") {
    sequenceWithAutoCycle(createGame()(p0))(
      RegisterForGame(1)(p1).accepted,
      SetInstructions(validInstructionSequence)(p0).accepted,
      SetInstructions(validInstructionSequence)(p1).accepted,
      assert { game =>
        game.players.map(_.id) shouldBe List(p0, p1)
        game.cycle shouldBe 1
        for (player <- game.players) {
          player shouldBe a[RunningPlayer]
          val rp = player.asInstanceOf[RunningPlayer]
          rp.instructionSlots shouldBe Seq.empty
          rp.instructionOptions.map(_.count).sum should be >= Constants.minimalOptionsPerCycle
        }
        succeed
      },
      RegisterForGame(2)(p2).rejected(),
      SetInstructions(validInstructionSequence)(p0).accepted,
      SetInstructions(validInstructionSequence)(p1).accepted,
      assert { game =>
        game.players.map(_.id) shouldBe List(p0, p1)
        game.cycle shouldBe 2
        for (player <- game.players) {
          player shouldBe a[RunningPlayer]
          val rp = player.asInstanceOf[RunningPlayer]
          rp.instructionSlots shouldBe Seq.empty
          rp.instructionOptions.map(_.count).sum should be >= Constants.minimalOptionsPerCycle
        }
        succeed
      }
    )
  }

  test("should create logs for all actions in the right order") {
    val scenario = Scenario(10, 10, Seq(Position(0, 9)), List(Robot(0, Position(0, 0), Down)), List.empty, List.empty)

    val initialGame = sequenceWithAutoCycle(createGame(scenario)(p0))(
      clearHistory,
      forcedInstructions(p0)(MoveForward, MoveForward, MoveForward, MoveForward, MoveForward),
      assertRunningPlayer(p0)(_.robot shouldBe Robot(0, Position(0, 5), Down)),
      assertLog(
        _ shouldBe Seq(
          StartCycleEvaluation(0),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 0), Position(0, 1)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 1), Position(0, 2)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 2), Position(0, 3)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 3), Position(0, 4)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 4), Position(0, 5)))),
          FinishedCycleEvaluation(0)
        )
      ),
      clearHistory,
      forcedInstructions(p0)(MoveForward, MoveForward, MoveForward, MoveForward, Sleep),
      assertLog(
        _ shouldBe Seq(
          StartCycleEvaluation(1),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 5), Position(0, 6)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 6), Position(0, 7)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 7), Position(0, 8)))),
          RobotAction(0, MoveForward),
          RobotMoves(List(RobotPositionTransition(0, Down, Position(0, 8), Position(0, 9)))),
          RobotAction(0, Sleep),
          PlayerFinished(0, Robot(0, Position(0, 9), Down)),
          FinishedCycleEvaluation(1),
          AllPlayersFinished
        )
      )
    )
  }
}
