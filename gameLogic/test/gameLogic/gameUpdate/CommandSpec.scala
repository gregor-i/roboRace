package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CommandSpec extends FunSuite with Matchers with GameUpdateHelper {

  test("DefineScenario: set the scenario and the first player") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      assertStarting(_.players.map(_.name) shouldBe List(p1))
    )
  }

  test("DefineScenario: reject invalid scenarios") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default.copy(initialRobots = List.empty))(p1).rejected(InvalidScenario)
    )
  }

  test("RegisterForGame: add players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      assertStarting(_.players shouldBe List(StartingPlayer(0, p1, false), StartingPlayer(1, p2, false)))
    )
  }

  test("RegisterForGame: reject players with the same name") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered),
      assertStarting(_.players shouldBe List(StartingPlayer(0, p1, false)))
    )
  }

  test("RegisterForGame: reject players if there are to many") {
    val emptyScenario = GameScenario.default.copy(initialRobots = GameScenario.default.initialRobots.take(1))
    updateChain(InitialGame)(
      DefineScenario(emptyScenario)(p1).accepted.anyEvents,
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered),
      RegisterForGame(p2).rejected(TooMuchPlayersRegistered),
      assertStarting(_.players.size shouldBe 1),
      assertStarting(_.players.head.name shouldBe p1)
    )
  }

  test("DeregisterForGame: deregister players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      DeregisterForGame(p1).accepted.anyEvents,
      assertStarting(_.players shouldBe List(StartingPlayer(0, p2, false)))
    )
  }

  test("ReadyForGame: start game if all players are ready") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      ReadyForGame(p1).accepted.anyEvents,
      ReadyForGame(p2).accepted.anyEvents,
      assertRunning(_ => succeed)
    )
  }

  test("RageQuit: finish players in order") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.noEvents,
      ReadyForGame(p1).accepted.noEvents,
      ReadyForGame(p2).accepted.anyEvents,
      assertRunning(_.cycle shouldBe 0),
      RageQuit(p2).accepted.anyEvents,
      ChooseInstructions(0, 0 until Constants.instructionsPerCycle)(p1).accepted.anyEvents,
      assertRunning(_.cycle shouldBe 1),
      RageQuit(p1).accepted.anyEvents,
      assertFinished(_.players.find(_.name == p1).get.finished.get shouldBe FinishedStatistic(rank = 1, cycle = 1, rageQuitted = true)),
      assertFinished(_.players.find(_.name == p2).get.finished.get shouldBe FinishedStatistic(rank = 2, cycle = 0, rageQuitted = true))
    )
  }

}
