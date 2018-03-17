package gameLogic.action

case class ActionSlots(actions: Seq[Option[Action]]){
  def allDefined: Boolean = actions.forall(_.isDefined)
  def allEmpty: Boolean = actions.forall(_.isEmpty)
  def updated(index: Int, action: Option[Action]): ActionSlots = this.copy(actions = actions.updated(index, action))
}

object ActionSlots{
  val actionsPerCycle = 5
  val emptyActionSet = ActionSlots(actions = Seq.fill(actionsPerCycle)(None))
}

sealed trait Action

sealed trait TurnAction extends Action
sealed trait MoveAction extends Action

case object MoveForward extends MoveAction
case object MoveBackward extends MoveAction
case object TurnRight extends TurnAction
case object TurnLeft extends TurnAction
