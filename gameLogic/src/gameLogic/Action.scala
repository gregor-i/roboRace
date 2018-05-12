package gameLogic

sealed trait Action

sealed trait TurnAction extends Action
sealed trait MoveAction extends Action

case object MoveForward extends MoveAction
case object MoveBackward extends MoveAction
case object StepRight extends MoveAction
case object StepLeft extends MoveAction
case object MoveTwiceForward extends MoveAction

case object TurnRight extends TurnAction
case object TurnLeft extends TurnAction
case object UTurn extends TurnAction

case object Sleep extends Action

object Action{
  implicit val ordering: Ordering[Action] = Ordering.Int.on{
    case MoveForward => 1
    case MoveTwiceForward => 2
    case StepRight => 3
    case StepLeft => 4
    case MoveBackward => 5
    case TurnRight => 6
    case TurnLeft => 7
    case UTurn => 8
    case Sleep => 9
  }
}