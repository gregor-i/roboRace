package gameLogic.action

sealed trait Action
case object MoveForward extends Action
case object MoveBackward extends Action
case object TurnRight extends Action
case object TurnLeft extends Action
