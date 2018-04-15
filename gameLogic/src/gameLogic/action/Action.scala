package gameLogic.action

object ActionSlots{
  val actionsPerCycle = 5
}

sealed trait Action

sealed trait TurnAction extends Action
sealed trait MoveAction extends Action

case object MoveForward extends MoveAction
case object MoveBackward extends MoveAction
case object TurnRight extends TurnAction
case object TurnLeft extends TurnAction
