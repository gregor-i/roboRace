package gameLogic.command

import gameEntities._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class DeregisterForGameSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("deregister players") {
    sequenceWithAutoCycle(createGame()(p0))(
      RegisterForGame(1)(p1).accepted,
      DeregisterForGame(p0).accepted,
      assert(_.players.map(_.id) shouldBe List(p1))
    )
  }

  test("finish players in order") {
    sequenceWithAutoCycle(createGame()(p0))(
      RegisterForGame(1)(p1).accepted,
      RegisterForGame(2)(p2).accepted,
      DeregisterForGame(p2).accepted,
      forcedInstructions(p0)(),
      forcedInstructions(p1)(),
      assertCycle(1),
      DeregisterForGame(p1).accepted,
      assertQuittedPlayer(p1)(_ => succeed),
      forcedInstructions(p0)(),
      assertCycle(2),
      DeregisterForGame(p0).accepted,
      assertQuittedPlayer(p0)(_ => succeed)
    )
  }
}
