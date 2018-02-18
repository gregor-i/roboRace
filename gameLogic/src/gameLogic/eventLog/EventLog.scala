package gameLogic.eventLog

import gameLogic.{Direction, Position}
import gameLogic.action.Action
import gameLogic.command.Command

sealed trait EventLog
case class CommandAccepted(command: Command) extends EventLog

case class RobotAction(playerName: String, action: Action) extends EventLog
case class RobotPositionTransition(playerName: String, from: Position, to: Position) extends EventLog
case class RobotDirectionTransition(playerName: String, from: Direction, to: Direction) extends EventLog

case object AllPlayerDefinedActions extends EventLog
case class PlayerActionsExecuted(nextCycle: Int) extends EventLog

case object PlayerAlreadyRegistered extends EventLog
case object NoPlayersRegistered extends EventLog
case object GameAlreadyRunning extends EventLog
case object GameNotRunning extends EventLog