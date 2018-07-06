package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("should start game if all players are ready") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted,
      RegisterForGame(p3).accepted,
      dummyInstructions(0)(p1),
      dummyInstructions(0)(p2),
      dummyInstructions(0)(p3),
      assert{ game =>
        game.players.map(_.name) shouldBe List(p1, p2, p3)
        game.cycle shouldBe 1
        for (player <- game.players) {
          player.instructionSlots shouldBe Instruction.emptySlots
          player.finished shouldBe None
          player.robot shouldBe Scenario.default.initialRobots(player.index)
          player.instructionOptions.size shouldBe Constants.instructionOptionsPerCycle
        }
        succeed
      }
    )
  }

  test("should start the next cycle when all player choose their action") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted,
      RegisterForGame(p3).accepted,
      dummyInstructions(0)(p1),
      dummyInstructions(0)(p2),
      dummyInstructions(0)(p3),
      assert(_.cycle shouldBe 1),
      dummyInstructions(1)(p1),
      dummyInstructions(1)(p2),
      dummyInstructions(1)(p3),
      assert(_.cycle shouldBe 2),
      assert(_ => succeed)
    )
  }
}
