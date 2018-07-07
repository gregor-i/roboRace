package gameLogic

sealed trait EventLog

case class GameStarted() extends EventLog

case class RobotMoves(playerName: String, transitions: Seq[RobotPositionTransition]) extends EventLog
case class RobotTurns(playerName: String, to: Direction) extends EventLog
case class MovementBlocked(playerName: String, position: Position, direction: Direction) extends EventLog
case class RobotReset(playerName: String, to: Robot) extends EventLog
case class PlayerFinished(playerName: String, stats: FinishedStatistic) extends EventLog
case class PlayerRageQuitted(playerName: String) extends EventLog
case class PlayerJoinedGame(playerName: String) extends EventLog

case object AllPlayersFinished extends EventLog

case class RobotPositionTransition(playerName: String, to: Position)

sealed trait RejectionReason
case object InvalidScenario extends RejectionReason
case object PlayerAlreadyRegistered extends RejectionReason
case object PlayerNotRegistered extends RejectionReason
case object NoPlayersRegistered extends RejectionReason
case object TooMuchPlayersRegistered extends RejectionReason
case object PlayerAlreadyFinished extends RejectionReason
case object WrongCycle extends RejectionReason
case object PlayerNotFound extends RejectionReason
case object WrongState extends RejectionReason
case object InvalidActionChoice extends RejectionReason
case object ActionAlreadyUsed extends RejectionReason
case object InvalidSlot extends RejectionReason
