package gameLogic
package gameUpdate

import org.scalatest.{FunSuite, Matchers}

class CommandSpec extends FunSuite with Matchers with UpdateChain {

  test("RegisterForGame: add players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted
    ) shouldBe GameStarting(
      scenario = GameScenario.default,
      players = List(StartingPlayer(0, p1, false), StartingPlayer(1, p2, false))
    )
  }

  test("RegisterForGame: reject players with the same name") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p1).rejected(PlayerAlreadyRegistered)
    ) shouldBe GameStarting(
      scenario = GameScenario.default,
      players = List(StartingPlayer(0, p1, false))
    )
  }

  test("RegisterForGame: reject players if there are to many") {
    val emptyScenario = GameScenario.default.copy(initialRobots = List.empty)
    updateChain(InitialGame)(
      DefineScenario(emptyScenario)(p1).accepted,
      RegisterForGame(p1).rejected(TooMuchPlayersRegistered)
    ) shouldBe GameStarting(
      scenario = emptyScenario,
      players = List.empty
    )
  }

  test("DeregisterForGame: unregister players") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      DeregisterForGame(p1).accepted
    ) shouldBe GameStarting(scenario = GameScenario.default, players = List(StartingPlayer(0, p2, false)))
  }

  test("ReadyForGame: start game if all players are ready") {
    updateChain(InitialGame)(
      DefineScenario(GameScenario.default)(p1).accepted,
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      ReadyForGame(p1).accepted,
      ReadyForGame(p2).accepted
    ) shouldBe GameStarting(
      scenario = GameScenario.default,
      players = List(StartingPlayer(0, p1, true), StartingPlayer(1, p2, true))
    )
  }

}
