package gameLogic
package gameUpdate

import org.scalatest.{FunSuite, Matchers}

class CycleSpec extends FunSuite with Matchers with TestData {
  test("should start game if all players are ready") {
    val oldState = startingStateHelper(true, true, true)
    val state = Cycle(oldState)
    state.events shouldBe Seq(GameStarted())
    state.state shouldBe a[GameRunning]
    val started = state.state.asInstanceOf[GameRunning]
    started.players.map(_.name) shouldBe oldState.players.map(_.name)
    started.cycle shouldBe 0
    for(player <- started.players){
      player.actions shouldBe Seq()
      player.finished shouldBe None
      player.robot shouldBe s.initialRobots(player.index)
      player.possibleActions.size shouldBe Constants.actionOptionsPerCycle
    }
  }

  test("should not start a game if a player is not ready") {
    val oldState = startingStateHelper(true, true, true, false, true)
    val state = Cycle(oldState)
    state.events shouldBe Seq()
    state.state shouldBe oldState
  }
}
