package gameLogic.command

import gameLogic.{FinishedStatistic, Scenario}
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class DeregisterForGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("deregister players") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      DeregisterForGame(p0).accepted,
      assert(_.players.map(_.name) shouldBe List(p1))
    )
  }

  test("finish players in order") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      RegisterForGame(p1).accepted,
      RegisterForGame(p2).accepted,
      DeregisterForGame(p2).accepted,
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assertCycle(1),
      DeregisterForGame(p1).accepted,
      assertPlayer(p1)(_.finished shouldBe Some(FinishedStatistic(rank = 2, cycle = 1, rageQuitted = true))),
      forcedInstructions(p0)(),
      assertCycle(2),
      DeregisterForGame(p0).accepted,
      assertPlayer(p0)(_.finished shouldBe Some(FinishedStatistic(rank = 1, cycle = 2, rageQuitted = true)))
    )
  }

}
