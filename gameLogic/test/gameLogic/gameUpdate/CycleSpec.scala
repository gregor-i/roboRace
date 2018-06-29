package gameLogic
package gameUpdate

import gameLogic.StartNextCycle
import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with UpdateChain {
  test("should start game if all players are ready") {
    val c0 = updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      RegisterForGame(p3).accepted,
      ReadyForGame(p1).accepted,
      ReadyForGame(p2).accepted,
      ReadyForGame(p3).accepted,
      cycle.logged(_ shouldBe Seq(GameStarted()))
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

  test("should not start a game if a player is not ready") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      cycle.logged(_ shouldBe Seq()),
      RegisterForGame(p1).accepted,
      cycle.logged(_ shouldBe Seq()),
      RegisterForGame(p2).accepted,
      cycle.logged(_ shouldBe Seq()),
      RegisterForGame(p3).accepted,
      cycle.logged(_ shouldBe Seq()),
      ReadyForGame(p1).accepted,
      cycle.logged(_ shouldBe Seq()),
      ReadyForGame(p3).accepted,
      cycle.logged(_ shouldBe Seq())
    ) shouldBe a[GameStarting]
  }


  test("should start the next cycle when all player choose their action") {
    val c1 = updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      RegisterForGame(p3).accepted,
      ReadyForGame(p1).accepted,
      ReadyForGame(p2).accepted,
      ReadyForGame(p3).accepted,
      cycle.logged(_ shouldBe Seq(GameStarted())),
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p1).accepted,
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p2).accepted,
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p3).accepted,
      cycle.logged(_ should contain(StartNextCycle(1))),
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p1).accepted,
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p2).accepted,
      ChooseInstructions(1, 0 until Constants.instructionsPerCycle)(p3).accepted,
      cycle.logged(_ should contain(StartNextCycle(2)))
    ).asInstanceOf[GameRunning]
  }
}
