package helper

import gameLogic._
import gameLogic.gameUpdate._

trait UpdateChainHelper {
  def updateChain(state: Game)(fs: (Game => Game)*): Game =
    fs.foldLeft(state)((s, f) => Cycle(f(s)))
}
