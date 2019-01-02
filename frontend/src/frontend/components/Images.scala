package frontend.components

import gameEntities._

object Images {
  val logo: String = "/assets/logo.svg"

  val iconClose: String = "/assets/ic-close.svg"
  val iconGamerlist: String = "/assets/ic-gamerlist.svg"
  val iconReplayAnimation: String = "/assets/ic-replay-animation.svg"

  val tile: String = "/assets/tile.svg"

  val target: String = "/assets/target.svg"
  val targetInactive: String = "/assets/target-inactive.svg"

  def wall(wallDirection: WallDirection): String = wallDirection match {
    case Down => "/assets/wall-down.svg"
    case UpRight => "/assets/wall-up-right.svg"
    case DownRight => "/assets/wall-down-right.svg"
  }

  def player(index: Int): String = s"/assets/player${index+1}.svg"

  def playerStart(index: Int): String = s"/assets/player${index+1}-start.svg"

  def action(inst: Instruction): String = inst match {
    case MoveForward => "/assets/action-move-forward.svg"
    case MoveBackward => "/assets/action-move-backward.svg"
    // StepRight : "/assets/action_StepRight.svg",
    // StepLeft : "/assets/action_StepLeft.svg",
    case MoveTwiceForward => "/assets/action-move-forward-twice.svg"
    case TurnRight => "/assets/action-turn-right-60.svg"
    case TurnLeft => "/assets/action-turn-left-60.svg"
    case UTurn => "/assets/action-turn-left-180.svg"
    case Sleep => "/assets/action-sleep.svg"
  }

  def trapStun = "/assets/trap-stun.svg"
  def trapTurnRight = "/assets/trap-turn-right.svg"
  def trapTurnLeft = "/assets/trap-turn-left.svg"

  def trap(t: Trap): String = t match {
    case _: StunTrap => trapStun
    case _: TurnRightTrap => trapTurnRight
    case _: TurnLeftTrap => trapTurnLeft
  }
}
