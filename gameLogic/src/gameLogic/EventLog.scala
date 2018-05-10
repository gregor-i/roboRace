package gameLogic

sealed abstract class EventLog(val text: String)

case class GameStarted() extends EventLog("Game Started")

case class NextRobotForActionDefined(playerName: String) extends EventLog(s"'$playerName' is next.")
case class RobotAction(playerName: String, action: Action) extends EventLog(s"'$playerName' does '$action'")
case class RobotPositionTransition(playerName: String, from: Position, to: Position) extends EventLog(s"'$playerName' moves from $from to $to")
case class RobotDirectionTransition(playerName: String, from: Direction, to: Direction) extends EventLog(s"'$playerName' turns from $from to $to")
case class RobotMovementBlocked(playerName: String, position: Position, direction: Direction) extends EventLog(s"The movement of '$playerName' is blocked")
case class RobotReset(playerName: String, from: Robot, to: Robot) extends EventLog(s"'$playerName' fell from the board and was reseted")
case class PlayerFinished(playerName: String, stats: FinishedStatistic) extends EventLog(s"'$playerName' reached the targes as number ${stats.rank}")

case class StartNextCycle(cycle: Int) extends EventLog("starting next cycle")
case object AllPlayersFinished extends EventLog("All players reached the target")

sealed trait RejectionReason
case object PlayerAlreadyRegistered extends RejectionReason
case object NoPlayersRegistered extends RejectionReason
case object TooMuchPlayersRegistered extends RejectionReason
case object WrongCycle extends RejectionReason
case object PlayerNotFound extends RejectionReason
case object WrongState extends RejectionReason
case object InvalidActionChoice extends RejectionReason
