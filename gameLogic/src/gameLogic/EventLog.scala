package gameLogic

import gameLogic.gameUpdate.RobotPushed

sealed abstract class EventLog(val text: String)

case class GameStarted() extends EventLog("Game Started")

case class RobotAction(playerName: String, instruction: Instruction) extends EventLog(s"'$playerName' does '$instruction'")
case class RobotMoves(playerName: String, to: Position, push: Option[RobotPushed]) extends EventLog(s"'$playerName' moves to $to")
case class RobotTurns(playerName: String, to: Direction) extends EventLog(s"'$playerName' turns $to")
case class MovementBlocked(playerName: String, position: Position, direction: Direction) extends EventLog(s"The movement of '$playerName' is blocked")
case class RobotReset(playerName: String, to: Robot, cycle: Int) extends EventLog(s"'$playerName' fell from the board and was resetted to ${to.position}")
case class PlayerFinished(playerName: String, stats: FinishedStatistic) extends EventLog(s"'$playerName' reached the target as number ${stats.rank}")
case class PlayerRageQuitted(playerName: String) extends EventLog(s"'$playerName' has left the game")
case class PlayerJoinedGame(playerName: String) extends EventLog(s"'$playerName' has joined the game")

case class StartNextCycle(cycle: Int) extends EventLog("starting next cycle")
case object AllPlayersFinished extends EventLog("All players reached the target")

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
