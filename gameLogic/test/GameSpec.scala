import gameLogic.{GameRunning, GameState}
import gameLogic.command.{RegisterForGame, StartGame}
import gameLogic.eventLog.{GameStarted, PlayerRegisteredForGame}
import gameLogic.processor.Processor
import org.scalatest.{FunSpec, FunSuite, Matchers}

class GameSpec extends FunSuite with Matchers {
  test("start the game") {
    val protcolledState = Processor(GameState.initalState)(Seq(
      RegisterForGame(playerName = "player 1"),
      RegisterForGame(playerName = "player 2"),
      StartGame(null)
    ))

    protcolledState.state shouldBe GameRunning(0, null)
    protcolledState.events shouldBe Seq(PlayerRegisteredForGame("player 1"), PlayerRegisteredForGame("player 2"), GameStarted)
  }

}
