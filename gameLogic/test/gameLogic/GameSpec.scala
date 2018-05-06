package gameLogic

import gameLogic.gameUpdate._
import org.scalatest.{FunSuite, Matchers}

class GameSpec extends FunSuite with Matchers {

  private val scenario = GameScenario.default.copy(pits = Seq.empty)

  private val startGameActions = Seq(
    DefineScenario(scenario),
    RegisterForGame(playerName = "player 1"),
    RegisterForGame(playerName = "player 2"),
    StartGame
  )

  val options = DealOptions()

  private val cycle0State = GameRunning(cycle = 0, scenario = scenario,
    players = Seq(
      Player(index = 0, name = "player 1", robot = Robot(Position(1, 8), Up), actions = Seq.empty, finished = None, options),
      Player(index = 1, name = "player 2", robot = Robot(Position(5, 8), Up), actions = Seq.empty, finished = None, options)
    ))

  private val bothMoveForwardActions = for {
    player <- Seq("player 1", "player 2")
  } yield DefineNextAction(player, 0, Seq.fill(Constants.actionsPerCycle)(MoveForward))

  private val cycle1State = GameRunning(cycle = 1, scenario = scenario,
    players = Seq(
      Player(index = 0, name = "player 1", robot = Robot(Position(1, 3), Up), actions = Seq.empty, finished = None, options),
      Player(index = 1, name = "player 2", robot = Robot(Position(5, 3), Up), actions = Seq.empty, finished = None, options)
    ))

  test("start the game") {
    val loggedState = acceptAllCommands(GameNotDefined)(startGameActions)
    loggedState.state shouldBe cycle0State
  }

  test("players define their actions") {
    val protocoledState = acceptAllCommands(cycle0State)(bothMoveForwardActions)
    protocoledState.state shouldBe cycle1State
  }

  private def acceptAllCommands[A](initial: GameState)(commands: Seq[Command]): Logged[GameState] =
    if (commands.nonEmpty) {
      Processor(initial, commands.head) {
        newState => newState.flatMap(acceptAllCommands(_)(commands.tail))
      } {
        rejected => fail()
      }
    } else {
      Logged.pure(initial)
    }
}
