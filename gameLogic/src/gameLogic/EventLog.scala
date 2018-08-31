package gameLogic

sealed trait EventLog

case class PlayerJoinedGame(playerIndex: Int, robot: Robot) extends EventLog

case class NextCycle(nextCycle: Int) extends EventLog

case class RobotAction(playerIndex: Int, instruction: Instruction) extends EventLog
case class RobotReset(playerIndex: Int, to: Robot) extends EventLog

case class RobotMoves(transitions: Seq[RobotPositionTransition]) extends EventLog
  case class RobotPositionTransition(playerIndex: Int, from: Position, to: Position)
case class RobotTurns(playerIndex: Int, from: Direction, to: Direction) extends EventLog
case class MovementBlocked(playerIndex: Int, robot: Robot) extends EventLog

case class PlayerFinished(playerIndex: Int) extends EventLog
case class PlayerRageQuitted(playerIndex: Int) extends EventLog

case object AllPlayersFinished extends EventLog

