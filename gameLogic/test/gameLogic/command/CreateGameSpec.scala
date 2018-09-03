package gameLogic.command

import gameLogic.{PlayerJoinedGame, Scenario}
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CreateGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("set the scenario and the first player") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      assert(_.players.map(_.name) shouldBe List(p0)),
      assertLog(_ should contain(PlayerJoinedGame(0, Scenario.default.initialRobots.head)))
    )
  }

  test("reject invalid scenarios") {
    CreateGame(Scenario.default.copy(initialRobots = List.empty))(p0) shouldBe a[CommandRejected]
  }
}
