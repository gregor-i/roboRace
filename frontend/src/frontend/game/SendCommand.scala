package frontend.game

import frontend.Service
import gameEntities.Command

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SendCommand {
  def apply(gameState: GameState, command: Command): Future[GameState] =
    Service.sendCommand(gameState.game.id, command)
      .map(g => gameState.copy(game = g))
}
