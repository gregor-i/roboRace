package gameLogic
package gameUpdate

import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CommandSpec extends FunSuite with Matchers with GameUpdateHelper {

  test("RegisterForGame: add players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      assertStarting(_.players shouldBe List(StartingPlayer(0, p1, false), StartingPlayer(1, p2, false)))
    )
  }

  test("RegisterForGame: reject players with the same name") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p1).accepted.anyEvents,
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered),
      assertStarting(_.players shouldBe List(StartingPlayer(0, p1, false)))
    )
  }

  test("RegisterForGame: reject players if there are to many") {
    val emptyScenario = GameScenario.default.copy(initialRobots = List.empty)
    updateChain(InitialGame)(
      DefineScenario(emptyScenario)(p1).accepted.anyEvents,
      RegisterForGame(p1).rejected(TooMuchPlayersRegistered),
      assertStarting(_.players shouldBe List.empty)
    )
  }

  test("DeregisterForGame: deregister players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      DeregisterForGame(p1).accepted.anyEvents,
      assertStarting(_.players shouldBe List(StartingPlayer(0, p2, false)))
    )
  }

  test("ReadyForGame: start game if all players are ready") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted.anyEvents,
      RegisterForGame(p1).accepted.anyEvents,
      RegisterForGame(p2).accepted.anyEvents,
      ReadyForGame(p1).accepted.anyEvents,
      ReadyForGame(p2).accepted.anyEvents,
      assertRunning(_ => succeed)
    )
  }

}
