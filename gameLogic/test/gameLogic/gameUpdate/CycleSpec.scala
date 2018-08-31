package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("should execute actions if all players are entered their action") {
    updateChain(createGame(Scenario.default)(p0))(
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
}
