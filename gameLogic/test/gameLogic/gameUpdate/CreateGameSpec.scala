package gameLogic.gameUpdate

import gameLogic.Scenario
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CreateGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("set the scenario and the first player") {
    updateChain(createGame(Scenario.default)(p0))(
      assert(_.players.map(_.name) shouldBe List(p0))
    )
  }

  test("reject invalid scenarios") {
    CreateGame(Scenario.default.copy(initialRobots = List.empty))(p0) shouldBe a[CommandRejected]
  }
}
