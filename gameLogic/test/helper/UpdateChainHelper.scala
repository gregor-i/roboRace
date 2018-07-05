package helper

import gameLogic.gameUpdate._
import gameLogic.{Game, Scenario}
import org.scalatest.Matchers

trait UpdateChainHelper {
  _: Matchers =>
  def createGame(scenario: Scenario)(player: String): Game = {
    val resp = CreateGame(scenario)(player)
    resp shouldBe a[CommandAccepted]
    resp.asInstanceOf[CommandAccepted].newState
  }

  def updateChain(state: Game)(fs: (Game => Game)*): Game =
    fs.foldLeft(state)((s, f) => f(s))
}
