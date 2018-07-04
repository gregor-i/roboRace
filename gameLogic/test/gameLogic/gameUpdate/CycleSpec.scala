package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("should start game if all players are ready") {
    val c0 = updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.noEvents,
      RegisterForGame(p3).accepted.noEvents,
      ReadyForGame(p1).accepted.noEvents,
      ReadyForGame(p2).accepted.noEvents,
      ReadyForGame(p3).accepted.logged(_ shouldBe Seq(GameStarted()))
    ).asInstanceOf[GameRunning]

    c0.players.map(_.name) shouldBe List(p1, p2, p3)
    c0.cycle shouldBe 0
    for (player <- c0.players) {
      player.instructions shouldBe Seq()
      player.finished shouldBe None
      player.robot shouldBe GameScenario.default.initialRobots(player.index)
      player.instructionOptions.size shouldBe Constants.instructionOptionsPerCycle
    }
  }

  test("should start the next cycle when all player choose their action") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.noEvents,
      RegisterForGame(p3).accepted.noEvents,
      ReadyForGame(p1).accepted.noEvents,
      ReadyForGame(p2).accepted.noEvents,
      ReadyForGame(p3).accepted.logged(_ shouldBe Seq(GameStarted())),
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p1).accepted.noEvents,
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p2).accepted.noEvents,
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p3).accepted
        .logged(_ should contain(StartNextCycle(1))),
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p1).accepted.noEvents,
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p2).accepted.noEvents,
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p3).accepted
        .logged(_ should contain(StartNextCycle(2))),
      assertRunning(_ => succeed)
    )
  }
}
