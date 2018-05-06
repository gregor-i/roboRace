package gameLogic

sealed trait Action

sealed trait TurnAction extends Action
sealed trait MoveAction extends Action

case object MoveForward extends MoveAction
case object MoveBackward extends MoveAction
case object MoveTwiceForward extends MoveAction

case object TurnRight extends TurnAction
case object TurnLeft extends TurnAction
case object UTurn extends TurnAction

case object Sleep extends Action

object Action{
  implicit val ordering: Ordering[Action] = Ordering.Int.on{
    case MoveForward => 1
    case MoveTwiceForward => 2
    case MoveBackward => 3
    case TurnRight => 4
    case TurnLeft => 5
    case UTurn => 6
    case Sleep => 7
  }
}