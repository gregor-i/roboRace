package helper

import gameLogic.gameUpdate.{CommandAccepted, CommandRejected, CommandResponse, Cycle}
import gameLogic.{EventLog, GameState, Logged, RejectionReason}
import org.scalatest.{Assertion, Matchers}

trait UpdateChainHelper {
  _: Matchers =>
  type ChainElement = GameState => GameState

  def updateChain(state: GameState)(fs: ChainElement*): GameState =
    fs.foldLeft(state)((s, f) => f(s))

}
