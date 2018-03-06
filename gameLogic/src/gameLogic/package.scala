package object gameLogic extends util.LoggedMonadSyntax {
  type Logged[A] = util.LoggedMonad[A, EventLog]
  val Logged = util.LoggedMonad
  type LoggedGameState = Logged[GameState]

  type Robots = Map[String, Robot]

  trait GameUpdate {
    def apply(state: GameState): Logged[GameState]
  }
}
