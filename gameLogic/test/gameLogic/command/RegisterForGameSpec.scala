package gameLogic.command

import gameLogic.Scenario
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class RegisterForGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("add players") {
    sequenceWithAutoCycle(createGame()(p0))(
      RegisterForGame(1)(p1).accepted,
      assert(_.players.map(_.name) shouldBe List(p0, p1))
    )
  }

  test("reject players with the same name") {
    sequenceWithAutoCycle(createGame()(p0))(
      RegisterForGame(1)(p0).rejected(PlayerAlreadyRegistered),
      assert(_.players.map(_.name) shouldBe List(p0))
    )
  }

  test("reject players if there are to many") {
    val smallScenario = Scenario.default.copy(initialRobots = Scenario.default.initialRobots.take(1))
    sequenceWithAutoCycle(createGame(smallScenario)(p0))(
      RegisterForGame(1)(p0).rejected(PlayerAlreadyRegistered),
      RegisterForGame(2)(p1).rejected(InvalidIndex),
      assert(_.players.size shouldBe 1),
      assert(_.players.head.name shouldBe p0)
    )
  }
}
