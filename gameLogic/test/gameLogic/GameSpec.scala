package gameLogic

import gameLogic.action.{MoveForward}
import gameLogic.gameUpdate.{DefineNextAction, DefineScenario, RegisterForGame, StartGame}
import org.scalatest.{FunSuite, Matchers}

class GameSpec extends FunSuite with Matchers {

  private val startGameActions = Seq(
    DefineScenario(GameScenario.default),
    RegisterForGame(playerName = "player 1"),
    RegisterForGame(playerName = "player 2"),
    StartGame
  )

  private val cycle0State = GameRunning(cycle = 0,
    players = Seq("player 1", "player 2"),
    robotActions = Map.empty,
    scenario = GameScenario.default,
    robots = Map("player 1" -> Robot(Position(1, 8), Up), "player 2" -> Robot(Position(5, 8), Up)))

  private val bothMoveForwardActions = for {
    slot <- 0 until ActionSlots.actionsPerCycle
    player <- Seq("player 1", "player 2")
  } yield DefineNextAction(player, 0, slot, Some(MoveForward))

  private val cycle1State = GameRunning(cycle = 1,
    players = Seq("player 1", "player 2"),
    robotActions = Map.empty,
    scenario = GameScenario.default,
    robots = Map("player 1" -> Robot(Position(1, 3), Up), "player 2" -> Robot(Position(5, 3), Up)))

  test("start the game") {
    val protocoledState = Processor(GameState.initalState)(startGameActions)
    protocoledState.state shouldBe cycle0State
    protocoledState.events shouldBe startGameActions.map(CommandAccepted)
  }

  test("players define their actions") {
    val protocoledState = Processor(cycle0State)(bothMoveForwardActions)
    protocoledState.state shouldBe cycle1State
    for (command <- bothMoveForwardActions)
      protocoledState.events should contain(CommandAccepted(command))
  }

}
