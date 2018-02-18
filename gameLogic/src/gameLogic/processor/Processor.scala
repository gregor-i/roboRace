package gameLogic
package processor

import gameLogic.command.Command
import gameLogic.eventLog.{Logged, LoggedSyntax}

object Processor extends LoggedSyntax {
  def apply(gameState: GameState)(commands: Seq[Command]): GameLogged[GameState] =
    commands.foldLeft[GameLogged[GameState]](Logged.pure(gameState))((state, command) => state.flatMap(command))
}
