package helper

import gameLogic._
import gameLogic.gameUpdate._

trait UpdateChainHelper {
  def sequenceWithAutoCycle(state: Game)(fs: (Game => Game)*): Game =
    State.sequence(fs.map(_ andThen Cycle.apply): _*)(state)
}
