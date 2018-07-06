package helper

import gameLogic.gameUpdate._
import gameLogic.{Constants, Game, Scenario}
import org.scalatest.Matchers

trait UpdateChainHelper {
  _: Matchers with DeconstructHelper =>
  def createGame(scenario: Scenario)(player: String): Game = {
    val resp = CreateGame(scenario)(player)
    resp shouldBe a[CommandAccepted]
    resp.asInstanceOf[CommandAccepted].newState
  }

  def dummyInstructions(cycle: Int): String => Game => CommandResponse =
    player => ChooseInstructions(cycle, (0 until Constants.instructionsPerCycle).map(Some.apply))(player)

  def updateChain(state: Game)(fs: (Game => Game)*): Game =
    fs.foldLeft(state)((s, f) => f(s))
}
