package helper

import entities.Game
import logic.State
import logic.gameUpdate.Cycle

trait UpdateChainHelper {
  def sequenceWithAutoCycle(state: Game)(fs: (Game => Game)*): Game =
    State.sequence(fs.map(_ andThen Cycle): _*)(state)
}
