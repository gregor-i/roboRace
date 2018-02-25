import gameLogic.action.MoveForward
import gameLogic.{GameRunning, GameScenario, GameState, Position, Robot, Up}
import gameLogic.command.{DefineNextAction, RegisterForGame, StartGame}
import gameLogic.eventLog.CommandAccepted
import gameLogic.processor.Processor
import org.scalatest.{FunSuite, Matchers}

class GameSpec extends FunSuite with Matchers {

  private val startGameActions = Seq(
    RegisterForGame(playerName = "player 1"),
    RegisterForGame(playerName = "player 2"),
    StartGame(GameScenario.default)
  )

  private val cycle0State = GameRunning(cycle = 0,
    players = Seq("player 1", "player 2"),
    robotActions = Map.empty,
    scenario = GameScenario.default,
    robots = Map("player 1" -> Robot(Position(1, 8), Up), "player 2" -> Robot(Position(5, 8), Up)))

  private val bothMoveForewardActions = Seq(
    DefineNextAction("player 2", 0, MoveForward),
    DefineNextAction("player 1", 0, MoveForward)
  )

  private val cycle1State = GameRunning(cycle = 1,
    players = Seq("player 1", "player 2"),
    robotActions = Map.empty,
    scenario = GameScenario.default,
    robots = Map("player 1" -> Robot(Position(1, 7), Up), "player 2" -> Robot(Position(5, 7), Up)))

  test("start the game") {
    val protocoledState = Processor(GameState.initalState)(startGameActions)
    protocoledState.state shouldBe cycle0State
    protocoledState.events shouldBe startGameActions.map(CommandAccepted)
  }

  test("players define their actions") {
    val protocoledState = Processor(cycle0State)(bothMoveForewardActions)
    protocoledState.state shouldBe cycle1State
    for (command <- bothMoveForewardActions)
      protocoledState.events should contain(CommandAccepted(command))
  }

}
