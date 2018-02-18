package gameLogic.eventLog

sealed trait EventLog
case class PlayerRegisteredForGame(playName: String) extends EventLog
case object PlayerAlreadyRegistered extends EventLog

case object NoPlayersRegistered extends EventLog
case object GameStarted extends EventLog
case object GameAlreadyRunning extends EventLog