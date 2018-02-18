import gameLogic.eventLog.{EventLog, Logged, LoggedSyntax}

package object gameLogic extends LoggedSyntax {
  type LoggedGameState = Logged[GameState, EventLog]

  trait GameUpdate {
    def apply(state: GameState): LoggedGameState
  }
}
