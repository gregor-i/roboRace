package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("should execute actions if all players are entered their action") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assert{ game =>
        game.players.map(_.name) shouldBe List(p0, p1)
        game.cycle shouldBe 1
        for (player <- game.players) {
          player.instructionSlots shouldBe Instruction.emptySlots
          player.finished shouldBe None
          player.instructionOptions.size shouldBe Constants.instructionOptionsPerCycle
        }
        succeed
      },
      RegisterForGame(p2).rejected(),
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assert{ game =>
        game.players.map(_.name) shouldBe List(p0, p1)
        game.cycle shouldBe 2
        for (player <- game.players) {
          player.instructionSlots shouldBe Instruction.emptySlots
          player.finished shouldBe None
          player.instructionOptions.size shouldBe Constants.instructionOptionsPerCycle
        }
        succeed
      }
    )
  }

  test("should create logs for all actions in the right order") {
    val scenario = Scenario(10, 10,
      Position(0, 9),
      List(Robot(Position(0, 0), Down)),
      List.empty, List.empty)

    val initialGame = sequenceWithAutoCycle(createGame(scenario)(p0))(
      clearHistory,
      forcedInstructions(p0)(MoveForward, MoveForward, MoveForward, MoveForward, MoveForward),
      assertPlayer(p0)(_.robot shouldBe Robot(Position(0, 5), Down)),
      assertLog(_ shouldBe Seq(
        StartCycleEvaluation(0),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,0), Position(0, 1)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,1), Position(0, 2)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,2), Position(0, 3)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,3), Position(0, 4)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,4), Position(0, 5)))),
        FinishedCycleEvaluation(0)
      )),
      clearHistory,
      forcedInstructions(p0)(MoveForward, MoveForward, MoveForward, MoveForward, Sleep),
      assertLog(_ shouldBe Seq(
        StartCycleEvaluation(1),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,5), Position(0, 6)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,6), Position(0, 7)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,7), Position(0, 8)))),
        RobotAction(0, MoveForward),
        RobotMoves(List(RobotPositionTransition(0, Position(0,8), Position(0, 9)))),
        RobotAction(0, Sleep),
        PlayerFinished(0),
        FinishedCycleEvaluation(1),
        AllPlayersFinished
      ))
    )
  }
}
