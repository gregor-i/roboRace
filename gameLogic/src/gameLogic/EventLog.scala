package gameLogic

sealed trait EventLog

case class PlayerJoinedGame(playerIndex: Int, robot: Robot) extends EventLog

case class StartCycleEvaluation(cycle: Int) extends EventLog
case class FinishedCycleEvaluation(cycle: Int) extends EventLog

case class RobotAction(playerIndex: Int, instruction: Instruction) extends EventLog
case class RobotReset(playerIndex: Int, from: Robot, to: Robot) extends EventLog

case class RobotMoves(transitions: Seq[RobotPositionTransition]) extends EventLog
  case class RobotPositionTransition(playerIndex: Int, direction: Direction, from: Position, to: Position)
case class RobotTurns(playerIndex: Int, position: Position, from: Direction, to: Direction) extends EventLog
case class MovementBlocked(playerIndex: Int, robot: Robot) extends EventLog

case class TrapEffect(playerIndex: Int, trap: Trap) extends EventLog

case class PlayerFinished(playerIndex: Int, robot:Robot) extends EventLog
case class PlayerRageQuitted(playerIndex: Int) extends EventLog

case object AllPlayersFinished extends EventLog

