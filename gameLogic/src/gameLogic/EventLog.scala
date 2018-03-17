package gameLogic

import gameLogic.action.Action
import gameLogic.gameUpdate.Command

sealed trait EventLog
case class CommandAccepted(command: Command) extends EventLog
case class CommandRejected(command: Command, reason: RejectionReason) extends EventLog

case class GameStateTransition(oldState: GameState, newState: GameState) extends EventLog

case class GameScenarioDefined(scenario: GameScenario) extends EventLog

case class NextRobotForActionDefined(playerName: String, weights: Map[String, (Int, Double, Double)]) extends EventLog
case class RobotAction(playerName: String, action: Action) extends EventLog
case class RobotPositionTransition(playerName: String, from: Position, to: Position) extends EventLog
case class RobotDirectionTransition(playerName: String, from: Direction, to: Direction) extends EventLog
case class RobotMovementBlocked(playerName: String, position: Position, direction: Direction) extends EventLog
case class RobotReset(playerName: String, from: Robot, to: Robot) extends EventLog

case object AllPlayerDefinedActions extends EventLog
case class PlayerActionsExecuted(nextCycle: Int) extends EventLog

sealed trait RejectionReason
case object PlayerAlreadyRegistered extends RejectionReason
case object NoPlayersRegistered extends RejectionReason
case object TooMuchPlayersRegistered extends RejectionReason
case object WrongCycle extends RejectionReason
case object PlayerNotFound extends RejectionReason
case object WrongState extends RejectionReason
case object InvalidActionSlot extends RejectionReason
