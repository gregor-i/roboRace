package roborace.frontend.game

import roborace.frontend.{GameFrontendState, Service}
import gameEntities.{Command, Game}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SendCommand {
  def apply(gameState: GameFrontendState, command: Command): Future[GameFrontendState] =
    Service
      .sendCommand(gameState.game.id, command)
      .map(g => roborace.frontend.game.Game.newCycleEffects(gameState, gameState.copy(game = g)))
}
