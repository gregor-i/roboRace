import gameLogic.eventLog.{EventLog, Logged, LoggedSyntax}

package object gameLogic extends LoggedSyntax{
  type GameLogged[A] = Logged[A, EventLog]
}
