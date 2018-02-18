package gameLogic
package processor

import gameLogic.command.{Command, Cycle}
import gameLogic.eventLog.{Logged, LoggedSyntax}

object Processor extends LoggedSyntax {
  def apply(gameState: GameState)(commands: Seq[Command]): LoggedGameState =
    commands.foldLeft[LoggedGameState](Logged.pure(gameState)){
      (state, command) =>
        for {
          beforeCommand <- state
          afterCommand <- command(beforeCommand)
          afterCycle <- Cycle(afterCommand)
        } yield afterCycle
    }
}
