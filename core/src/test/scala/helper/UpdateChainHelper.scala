package helper

import gameEntities.Game
import gameLogic.State
import gameLogic.gameUpdate.Cycle

trait UpdateChainHelper {
  def sequenceWithAutoCycle(state: Game)(fs: (Game => Game)*): Game =
    State.sequence(fs.map(_ andThen Cycle): _*)(state)
}
