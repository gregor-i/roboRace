package gameLogic.gameUpdate

import gameLogic.{GameState, Logged}

object Processor {
  def apply[A](gameState: GameState, command: Command)
              (ifAccepted: Logged[GameState] => A)(ifRejected: CommandRejected => A): A =
    command(gameState) match {
      case accepted: CommandAccepted => ifAccepted(Cycle(accepted.newState))
      case rejected: CommandRejected => ifRejected(rejected)
    }
}