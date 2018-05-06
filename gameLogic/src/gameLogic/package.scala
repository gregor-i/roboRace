package object gameLogic extends util.LoggedMonadSyntax {
  type Logged[A] = util.LoggedMonad[A, EventLog]
  val Logged = util.LoggedMonad
}
