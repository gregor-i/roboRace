package gameLogic
package processor

import gameLogic.gameUpdate.{Command, Cycle}

object Processor {
  def apply(gameState: GameState)(commands: Seq[Command]): Logged[GameState] =
    commands.foldLeft[Logged[GameState]](Logged.pure(gameState)){
      (state, command) =>
        for {
          beforeCommand <- state
          afterCommand <- command(beforeCommand)
          afterCycle <- Cycle(afterCommand)
        } yield afterCycle
    }
}
