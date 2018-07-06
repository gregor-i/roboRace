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

  def dummyInstructions(cycle: Int): String => Game => Game =
    player => game => updateChain(game)(
      (0 until Constants.instructionsPerCycle).map(i =>
        SetInstruction(cycle, i, i)(player).accepted.anyEvents
      ):_*
    )

  def updateChain(state: Game)(fs: (Game => Game)*): Game =
    fs.foldLeft(state)((s, f) => f(s))
}
