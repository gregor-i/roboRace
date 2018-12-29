package gameLogic.command

import gameEntities.{CommandRejected, PlayerJoinedGame}
import gameLogic.DefaultScenario
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class CreateGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("set the scenario and the first player") {
    sequenceWithAutoCycle(createGame()(p0))(
      assert(_.players.map(_.name) shouldBe List(p0)),
      assertLog(_ should contain(PlayerJoinedGame(0, DefaultScenario.default.initialRobots.head)))
    )
  }

  test("reject invalid scenarios") {
    CreateGame(DefaultScenario.default.copy(initialRobots = List.empty), 0)(p0) shouldBe a[CommandRejected]
  }
}
