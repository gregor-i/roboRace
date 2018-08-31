package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}
class CommandSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("RegisterForGame: add players") {
    updateChain(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      assert(_.players.map(_.name) shouldBe List(p0,p1))
    )
  }

  test("RegisterForGame: reject players with the same name") {
    updateChain(createGame(Scenario.default)(p0))(
      RegisterForGame(p0).rejected(PlayerAlreadyRegistered),
      assert(_.players.map(_.name) shouldBe List(p0))
    )
  }

  test("RegisterForGame: reject players if there are to many") {
    val smallScenario = Scenario.default.copy(initialRobots = Scenario.default.initialRobots.take(1))
    updateChain(createGame(smallScenario)(p0))(
      RegisterForGame(p0).rejected(PlayerAlreadyRegistered),
      RegisterForGame(p1).rejected(TooMuchPlayersRegistered),
      assert(_.players.size shouldBe 1),
      assert(_.players.head.name shouldBe p0)
    )
  }

  test("DeregisterForGame: deregister players") {
    updateChain(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      DeregisterForGame(p0).accepted,
      assert(_.players.map(_.name) shouldBe List(p1))
    )
  }

  test("ReadyForGame: start game if all players are ready") {
    updateChain(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assertCycle(1)
    )
  }

  test("RageQuit: finish players in order") {
    updateChain(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      DeregisterForGame(p2).accepted,
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assertCycle(1),
      DeregisterForGame(p1).accepted,
      forcedInstructions(p0)(),
      assertCycle(2),
      DeregisterForGame(p0).accepted,
      assertPlayer(p0)(_.finished shouldBe Some(FinishedStatistic(rank = 1, cycle = 2, rageQuitted = true))),
      assertPlayer(p1)(_.finished shouldBe Some(FinishedStatistic(rank = 2, cycle = 1, rageQuitted = true)))
    )
  }

}
