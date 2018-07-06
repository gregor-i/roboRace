package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CommandSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("RegisterForGame: add players") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted.anyEvents,
      assert(_.players.map(_.name) shouldBe List(p1,p2))
    )
  }

  test("RegisterForGame: reject players with the same name") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered),
      assert(_.players.map(_.name) shouldBe List(p1))
    )
  }

  test("RegisterForGame: reject players if there are to many") {
    val smallScenario = Scenario.default.copy(initialRobots = Scenario.default.initialRobots.take(1))
    updateChain(createGame(smallScenario)(p1))(
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered),
      RegisterForGame(p2).rejected(TooMuchPlayersRegistered),
      assert(_.players.size shouldBe 1),
      assert(_.players.head.name shouldBe p1)
    )
  }

  test("DeregisterForGame: deregister players") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted.anyEvents,
      DeregisterForGame(p1).accepted.anyEvents,
      assert(_.players.map(_.name) shouldBe List(p2))
    )
  }

  test("ReadyForGame: start game if all players are ready") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted.anyEvents,
      dummyInstructions(0)(p1).accepted.noEvents,
      dummyInstructions(0)(p2).accepted
        .logged(_ should contain(StartNextCycle(1))),
      assert(_ => succeed)
    )
  }

  test("RageQuit: finish players in order") {
    updateChain(createGame(Scenario.default)(p1))(
      RegisterForGame(p2).accepted.noEvents,
      RegisterForGame(p3).accepted.noEvents,
      DeregisterForGame(p3).accepted.anyEvents,
      dummyInstructions(0)(p1).accepted.noEvents,
      dummyInstructions(0)(p2).accepted
        .logged(_ should contain(StartNextCycle(1))),
      assert(_.cycle shouldBe 1),
      DeregisterForGame(p2).accepted.anyEvents,
      dummyInstructions(1)(p1).accepted.anyEvents,
      assert(_.cycle shouldBe 2),
      DeregisterForGame(p1).accepted.anyEvents,
      assert(_.players.find(_.name == p1).get.finished.get shouldBe FinishedStatistic(rank = 1, cycle = 2, rageQuitted = true)),
      assert(_.players.find(_.name == p2).get.finished.get shouldBe FinishedStatistic(rank = 2, cycle = 1, rageQuitted = true))
    )
  }

}
